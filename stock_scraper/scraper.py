"""
scraper.py
----------
Core scraping logic for Screener.in stock screens.

Exposes three reusable functions:
    open_query(page, query_url)      -> navigates to the screen and waits
                                         for the results table to load.
    extract_companies(page)          -> extracts every row from the table
                                         (across all pages, if paginated),
                                         auto-detecting column positions.
    save_json(data, filepath)        -> saves extracted data to a JSON file.
"""

import json
import re
from typing import Dict, List

from playwright.sync_api import Page, TimeoutError as PlaywrightTimeoutError

# Keywords used to auto-detect which table column holds which piece of data.
# Screener's column headers vary slightly depending on the query, so we
# match on keywords rather than assuming fixed indexes.
COLUMN_KEYWORDS = {
    "name": ["name"],
    "price": ["cmp", "current price", "price"],
    "market_cap": ["mar cap", "market cap", "mcap"],
    "pe": ["p/e", "pe"],
    "roce": ["roce"],
}

# Safety-net fallback: Screener's default "raw screen" column order, used
# ONLY if live header detection fails completely (e.g. the header row isn't
# exposed the way we expect via thead/th). This mirrors the standard order:
# S.No. | Name | CMP Rs. | P/E | Mar Cap Rs.Cr. | Div Yld % | NP Qtr Rs.Cr. |
# Qtr Profit Var % | Sales Qtr Rs.Cr. | Qtr Sales Var % | ROCE %
FALLBACK_HEADER_ORDER = [
    "S.No.", "Name", "CMP Rs.", "P/E", "Mar Cap Rs.Cr.", "Div Yld %",
    "NP Qtr Rs.Cr.", "Qtr Profit Var %", "Sales Qtr Rs.Cr.",
    "Qtr Sales Var %", "ROCE %",
]


def open_query(page: Page, query_url: str, timeout: int = 30000) -> None:
    """
    Navigate to a Screener.in query/screen URL and wait until the results
    table has fully loaded.

    Args:
        page: An active, already-authenticated Playwright Page object.
        query_url: The full Screener screen query URL.
        timeout: Max wait time (ms).

    Raises:
        RuntimeError: If the results table never appears.
    """
    try:
        print(f"[scraper] Navigating to query URL:\n{query_url}")
        page.goto(query_url, wait_until="domcontentloaded", timeout=timeout)

        print("[scraper] Waiting for results table to load...")
        table = page.locator("table.data-table").first
        table.wait_for(state="visible", timeout=timeout)

        # Wait explicitly until at least one data row is present in the
        # table body (handles the case where the table shell loads before
        # the AJAX-populated rows do). We check ANY table.data-table on the
        # page, since Screener.in pages can contain more than one element
        # with this class (e.g. a small peer-comparison widget elsewhere).
        page.wait_for_function(
            """() => {
                const tables = Array.from(document.querySelectorAll('table.data-table'));
                return tables.some(t => {
                    const tbody = t.querySelector('tbody');
                    return tbody && tbody.querySelectorAll('tr').length > 0;
                });
            }""",
            timeout=timeout,
        )

        print("[scraper] Results table loaded successfully.")

    except PlaywrightTimeoutError as e:
        raise RuntimeError(
            f"Timed out waiting for results table to load: {e}"
        ) from e
    except Exception as e:
        raise RuntimeError(f"Failed to open query URL: {e}") from e


def _get_results_table(page: Page):
    """
    Return the Locator for the actual results table.

    Screener.in pages can contain more than one element matching
    'table.data-table' (e.g. small comparison widgets). We disambiguate by
    picking the one with the most rows in its <tbody>, since the real
    results table is always the largest.
    """
    tables = page.locator("table.data-table")
    count = tables.count()
    if count == 0:
        raise RuntimeError("No 'table.data-table' element found on the page.")

    if count == 1:
        return tables.first

    best_index = 0
    best_row_count = -1
    for i in range(count):
        row_count = tables.nth(i).locator("tbody tr").count()
        if row_count > best_row_count:
            best_row_count = row_count
            best_index = i

    return tables.nth(best_index)


def _detect_header_texts(table) -> List[str]:
    """
    Try several strategies (in order) to pull header cell text out of the
    results table, since Screener's markup for the header row isn't
    guaranteed to be a plain <thead><tr><th> structure.

    Falls back to a hardcoded, known-good column order if every live
    detection strategy comes up empty.
    """
    strategies = [
        ("thead th", table.locator("thead th")),
        ("thead td", table.locator("thead td")),
        ("th (anywhere in table)", table.locator("th")),
    ]

    for label, locator in strategies:
        n = locator.count()
        if n > 0:
            texts = [_clean_text(locator.nth(i).inner_text()) for i in range(n)]
            texts = [t for t in texts if t]  # drop blanks (e.g. icon-only cells)
            if texts:
                print(f"[scraper] Headers detected via '{label}': {texts}")
                return texts

    # Strategy: first <tr> of the table, whatever cell tag it uses.
    first_row_cells = table.locator("tr").first.locator("th, td")
    n = first_row_cells.count()
    if n > 0:
        texts = [_clean_text(first_row_cells.nth(i).inner_text()) for i in range(n)]
        texts = [t for t in texts if t]
        if texts:
            print(f"[scraper] Headers detected via 'first row cells': {texts}")
            return texts

    # Nothing worked live -- fall back to the known standard Screener order.
    print(
        "[scraper] WARNING: Could not detect headers live from the DOM. "
        "Falling back to the standard Screener raw-screen column order: "
        f"{FALLBACK_HEADER_ORDER}"
    )
    return FALLBACK_HEADER_ORDER


def _build_column_map(header_cells: List[str]) -> Dict[str, int]:
    """
    Given a list of header cell texts (in order), determine which column
    index corresponds to each field we care about (name, price, market_cap,
    pe, roce) by matching keywords instead of assuming fixed positions.
    """
    normalized = [h.strip().lower() for h in header_cells]
    column_map: Dict[str, int] = {}

    for field, keywords in COLUMN_KEYWORDS.items():
        for idx, header_text in enumerate(normalized):
            if any(keyword in header_text for keyword in keywords):
                column_map[field] = idx
                break

    missing = [f for f in COLUMN_KEYWORDS if f not in column_map]
    if missing:
        print(f"[scraper] WARNING: could not auto-detect columns for: {missing}. "
              f"Headers seen: {header_cells}")

    return column_map


def _clean_text(text: str) -> str:
    """Collapse whitespace/newlines in a cell's text into a single string."""
    return re.sub(r"\s+", " ", text or "").strip()


def _extract_rows_from_current_page(table, column_map: Dict[str, int]) -> List[Dict]:
    """Extract all data rows from the currently-loaded table page."""
    rows_data = []
    row_locator = table.locator("tbody tr")
    row_count = row_locator.count()

    for i in range(row_count):
        row = row_locator.nth(i)
        cells = row.locator("td")
        cell_count = cells.count()
        if cell_count == 0:
            continue  # Skip separator/empty rows if any.

        def get_cell_text(field: str) -> str:
            idx = column_map.get(field)
            if idx is None or idx >= cell_count:
                return ""
            return _clean_text(cells.nth(idx).inner_text())

        company = {
            "name": get_cell_text("name"),
            "current_price": get_cell_text("price"),
            "market_cap": get_cell_text("market_cap"),
            "pe": get_cell_text("pe"),
            "roce": get_cell_text("roce"),
        }

        # Skip rows that don't actually have a company name (e.g. stray
        # "Median"/footer rows some Screener tables include).
        if company["name"]:
            rows_data.append(company)

    return rows_data


def extract_companies(page: Page, timeout: int = 30000) -> List[Dict]:
    """
    Extract every company row from the results table, automatically
    detecting column positions from the table header. Handles pagination
    (clicks "Next" until no further pages remain).

    Args:
        page: Playwright Page currently showing a loaded results table.
        timeout: Max wait time (ms) for pagination waits.

    Returns:
        A list of dicts, one per company, with keys:
        name, current_price, market_cap, pe, roce.
    """
    all_companies: List[Dict] = []

    try:
        # --- Locate the actual results table (disambiguating from any
        #     other table.data-table elements that might exist) ---
        table = _get_results_table(page)

        # --- Determine column layout from the table header ---
        header_texts = _detect_header_texts(table)
        print(f"[scraper] Detected table headers: {header_texts}")

        column_map = _build_column_map(header_texts)
        print(f"[scraper] Column mapping (field -> index): {column_map}")

        page_num = 1
        while True:
            print(f"[scraper] Extracting rows from page {page_num}...")
            page_rows = _extract_rows_from_current_page(table, column_map)
            print(f"[scraper] Found {len(page_rows)} rows on page {page_num}.")
            all_companies.extend(page_rows)

            # Look for a "Next" pagination control. Screener typically shows
            # a link/button with text "Next" when more pages are available.
            next_button = page.locator("a:has-text('Next')").first
            if next_button.count() == 0:
                print("[scraper] No further pages found. Extraction complete.")
                break

            is_disabled = next_button.get_attribute("class") or ""
            if "disabled" in is_disabled.lower():
                print("[scraper] 'Next' button is disabled. Extraction complete.")
                break

            try:
                print("[scraper] Navigating to next page of results...")
                next_button.scroll_into_view_if_needed(timeout=timeout)
                next_button.click(timeout=timeout)

                # Explicit wait for the table to refresh with new rows.
                page.wait_for_load_state("networkidle", timeout=timeout)

                # Re-resolve the table locator: pagination may swap the
                # underlying DOM element out entirely.
                table = _get_results_table(page)
                page_num += 1
            except PlaywrightTimeoutError:
                print("[scraper] Could not navigate to next page (timeout). Stopping pagination.")
                break

        print(f"[scraper] Total companies extracted: {len(all_companies)}")
        return all_companies

    except Exception as e:
        raise RuntimeError(f"Failed to extract companies: {e}") from e


def save_json(data: List[Dict], filepath: str = "companies.json") -> None:
    """
    Save extracted company data to a JSON file.

    Args:
        data: List of company dicts.
        filepath: Destination path for the JSON file.

    Raises:
        RuntimeError: If the file cannot be written.
    """
    try:
        print(f"[scraper] Saving {len(data)} companies to '{filepath}'...")
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=4, ensure_ascii=False)
        print(f"[scraper] Data successfully saved to '{filepath}'.")
    except Exception as e:
        raise RuntimeError(f"Failed to save JSON file: {e}") from e
