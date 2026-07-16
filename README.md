# 📈 Stock Research & Quarterly Analysis Platform

A personal investment research platform that helps you identify, track, and evaluate
fundamentally strong companies — built around real quarterly financial data instead of gut
feel or noisy market chatter.

---

## Table of Contents

- [Why This Exists](#why-this-exists)
- [What It Does](#what-it-does)
- [Who It's For](#whos-it-for)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Design Principles](#design-principles)
- [License](#license)

---

## Why This Exists

Most investors either drown in data or rely on gut feel. Scanning thousands of stocks every
quarter isn't practical for an individual, and generic screeners don't capture the specific
things you actually care about. This platform takes a narrower, more disciplined approach:
maintain a **curated list of companies worth watching**, and build a rigorous, quarter-over-
quarter picture of their financial health — so decisions are grounded in trends, not a single
snapshot.

## What It Does

**Company & Quarterly Data Management**
Maintain a personal database of companies you track, along with a full history of their
quarterly financial results — revenue, profit, and the other fundamentals that matter.

**Trend-Based Analysis**
Go beyond a single quarter's numbers. Compare performance across time to see whether a
company's fundamentals are genuinely improving, holding steady, or deteriorating.

**Fast, Focused Search**
Find any tracked company instantly by name or symbol, without digging through spreadsheets or
scattered notes.

**Financial Ratio Intelligence**
Automatically calculate the ratios that matter for quality investing — ROE, ROCE,
Debt-to-Equity, Profit Margins — so you spend time interpreting, not computing.

**Visual, Data-Driven Dashboards**
See revenue, profit, and return trends as charts, not rows in a table, making it far easier to
spot inflection points and sustained growth.

**Automated Data Collection**
Reduce manual data entry through automated import pipelines that keep your tracked companies'
financials current with minimal effort.

**Custom Screening**
Define your own filtering rules to identify companies that meet your specific investment
criteria, and save them as reusable templates.

**Watchlists & Alerts**
Organize companies into themed watchlists and get notified when new quarterly results or
notable changes land.

**Historical Price & Risk Analysis**
Layer in historical price data to understand how fundamentals have actually translated into
returns — and at what level of risk.

**Company Scoring**
Get objective, composite scores — quality, growth, value, and momentum — that let you rank and
compare companies at a glance instead of re-deriving a view from scratch every time.

**AI-Assisted Research**
Ask natural-language questions about your tracked companies, get plain-English fundamental
summaries, and compare companies side by side without manually cross-referencing spreadsheets.

## Who It's For

This is a **personal investment companion**, purpose-built for an individual investor who wants
to:

- Apply consistent, repeatable criteria to every company they track.
- Understand a business's trajectory, not just its most recent quarter.
- Combine software engineering, financial analysis, and data-driven decision-making into a
  single tool they fully own and control.

## Tech Stack

**Backend:** Java, Spring Boot, Spring Data MongoDB, Maven
**Database:** MongoDB
**Frontend:** React, Material UI, React Router
**Data Collection:** Python, pandas, requests, BeautifulSoup, yfinance / NSE-compatible sources

## Architecture

```text
                 +----------------------+
                 |  Financial Data APIs |
                 +----------+-----------+
                            |
                        Python Importer
                            |
                     Spring Boot REST API
                            |
                     Business Validation
                            |
                         MongoDB
                            |
                     React Dashboard
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- MongoDB 6+ (local instance or Atlas)
- Node.js 18+ and npm
- Python 3.10+ (for data import scripts)

### Backend Setup

```bash
git clone <your-repo-url>
cd stock-research-platform/backend

# configure MongoDB connection
# src/main/resources/application.properties
# spring.data.mongodb.uri=mongodb://localhost:27017/stockresearch

mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Frontend Setup

```bash
cd stock-research-platform/frontend
npm install
npm start
```

The app will be available at `http://localhost:3000`.

## Project Structure

```text
stock-research-platform/
├── backend/
│   ├── src/main/java/.../company/
│   ├── src/main/java/.../quarterlydata/
│   └── src/main/resources/application.properties
├── frontend/
│   ├── src/components/
│   ├── src/pages/
│   └── src/services/
├── data-import/            # Python scripts
├── User_Stories_Backlog.xlsx
└── README.md
```

## Design Principles

- **Fundamentals over noise.** Every feature ties back to real financial data, not sentiment
  or speculation.
- **Trends over snapshots.** A single good quarter means little; the platform is built to
  reveal sustained direction.
- **Own your data, own your process.** No black-box recommendations — every score, ratio, and
  flag is transparent and explainable.
- **Built to last.** A clean, extensible codebase that can grow in capability without becoming
  unmaintainable.

## License

Personal project — license to be decided (add MIT/Apache-2.0 if you plan to open-source it).