# Screener.in Stock Scraper (Playwright Sync API)

Automates logging into [Screener.in](https://www.screener.in), running a
custom stock screen query, and extracting the results table into JSON.

## Project structure

```
stock_scraper/
├── main.py            # Entry point - orchestrates the full workflow
├── scraper.py          # open_query(), extract_companies(), save_json()
├── auth.py             # login()
├── .env                # Your Screener.in credentials (not committed)
├── requirements.txt    # Python dependencies
└── companies.json       # Output file (generated on each run)
```

## Setup

1. **Install dependencies** (Python 3.8+):
   ```bash
   pip install -r requirements.txt
   playwright install chromium
   ```

2. **Add your credentials** to `.env`:
   ```
   SCREENER_EMAIL=your_email@example.com
   SCREENER_PASSWORD=your_password_here
   ```

## Run

```bash
python main.py
```

The browser will open visibly (`headless=False`) so you can watch each step:
login → navigate to the screen query → wait for results → scrape → save
`companies.json` → print a formatted summary to the console.

## Notes

- **Column auto-detection**: `extract_companies()` reads the table's
  `<thead>` headers and matches them against keyword lists (e.g. "cmp" /
  "current price" for price, "mar cap" for market cap, "p/e" for PE, "roce"
  for ROCE) rather than hardcoding column positions. This keeps the script
  working even if Screener reorders/relabels columns for a given query.
- **Pagination**: if the screen result spans multiple pages, the scraper
  automatically clicks "Next" and continues extracting until no further
  pages are found.
- **Explicit waits**: the script uses Playwright's `wait_for`,
  `wait_for_url`, `wait_for_function`, and `expect_navigation` instead of
  fixed `time.sleep()` calls, so it adapts to actual page/network timing.
- **Error handling**: each stage (login, query navigation, extraction,
  saving) is wrapped in try/except blocks that raise clear `RuntimeError`
  messages, and `main.py` ensures the browser is always closed via a
  `finally` block.
- Never commit real credentials in `.env` to a public repository.
