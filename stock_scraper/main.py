"""
main.py
-------
Entry point for the Screener.in stock scraper.

Workflow:
    1. Load credentials from .env
    2. Launch Chromium (headless=False) via Playwright Sync API
    3. Log in (auth.login)
    4. Open the target query/screen URL (scraper.open_query)
    5. Extract all company rows (scraper.extract_companies)
    6. Save results to companies.json (scraper.save_json)
    7. Pretty-print a summary + full listing to the console
"""

import os
import sys

from dotenv import load_dotenv
from playwright.sync_api import sync_playwright

from auth import login
from scraper import open_query, extract_companies, save_json

# ----------------------------------------------------------------------
# Configuration
# ----------------------------------------------------------------------
QUERY_URL = (
    "https://www.screener.in/screen/raw/?sort=Market+Capitalization&order=desc"
    "&source_id=343087&query=YOY+Quarterly+sales+growth+%3E+15%25+AND%0D%0A"
    "YOY+Quarterly+sales+growth+%3C+100%25+AND%0D%0A"
    "YOY+Quarterly+profit+growth+%3E+20%25+AND%0D%0A"
    "YOY+Quarterly+profit+growth+%3C+150%25+AND%0D%0A"
    "Net+Profit+latest+quarter+%3E+Net+Profit+preceding+quarter+AND%0D%0A"
    "Net+Profit+preceding+quarter+%3E+Net+profit+2quarters+back+AND%0D%0A"
    "Net+profit+2quarters+back+%3E+Net+profit+3quarters+back+AND%0D%0A"
    "Net+Profit+latest+quarter+%3E+2+AND%0D%0A"
    "Current+price+%3C+50+AND%0D%0A"
    "Sales+growth+3Years+%3E+15%25+AND%0D%0A"
    "Profit+growth+3Years+%3E+15%25"
)

OUTPUT_FILE = "companies.json"
NAV_TIMEOUT_MS = 30000


def print_summary(companies):
    """Pretty-print the final results in the required format."""
    print("\n==================================")
    print(f"Total Companies : {len(companies)}")
    print("==================================\n")

    for idx, company in enumerate(companies, start=1):
        price = company.get("current_price", "") or "N/A"
        market_cap = company.get("market_cap", "") or "N/A"
        pe = company.get("pe", "") or "N/A"
        roce = company.get("roce", "") or "N/A"

        # Prefix with rupee symbol only if not already present in the value.
        price_display = price if price.startswith("₹") else f"₹{price}"
        mcap_display = market_cap if market_cap.startswith("₹") else f"₹{market_cap}"

        print(f"{idx}.")
        print(f"Company : {company.get('name', 'N/A')}")
        print(f"Price : {price_display}")
        print(f"Market Cap : {mcap_display}")
        print(f"PE : {pe}")
        print(f"ROCE : {roce}")
        print("----------------------------------")


def main():
    # --------------------------------------------------------------
    # 1. Load credentials from .env
    # --------------------------------------------------------------
    print("[main] Loading environment variables from .env ...")
    load_dotenv()

    email = os.getenv("SCREENER_EMAIL")
    password = os.getenv("SCREENER_PASSWORD")

    if not email or not password:
        print("[main] ERROR: SCREENER_EMAIL and SCREENER_PASSWORD must be set in .env")
        sys.exit(1)

    companies = []

    # --------------------------------------------------------------
    # 2. Launch browser and run the automation
    # --------------------------------------------------------------
    try:
        with sync_playwright() as p:
            print("[main] Launching Chromium browser (headless=False)...")
            browser = p.chromium.launch(headless=False)
            context = browser.new_context()
            page = context.new_page()
            page.set_default_timeout(NAV_TIMEOUT_MS)

            try:
                # ---- Step 1-3: Login ----
                login(page, email, password, timeout=NAV_TIMEOUT_MS)

                # ---- Step 4-5: Open query & wait for table ----
                open_query(page, QUERY_URL, timeout=NAV_TIMEOUT_MS)

                # ---- Step 6: Extract rows ----
                companies = extract_companies(page, timeout=NAV_TIMEOUT_MS)

                # ---- Step 9: Save to JSON ----
                save_json(companies, OUTPUT_FILE)

            except Exception as step_error:
                print(f"[main] ERROR during automation steps: {step_error}")
                raise

            finally:
                print("[main] Closing browser...")
                context.close()
                browser.close()

    except Exception as e:
        print(f"[main] FATAL ERROR: {e}")
        sys.exit(1)

    # --------------------------------------------------------------
    # 7-8: Print summary + full listing
    # --------------------------------------------------------------
    if companies:
        print_summary(companies)
    else:
        print("[main] No companies were extracted.")


if __name__ == "__main__":
    main()
