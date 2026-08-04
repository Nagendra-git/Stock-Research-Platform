from fastapi import FastAPI

from api.upstox_client import UpstoxClient
from collector.historical_data import HistoricalDataCollector
from indicators.indicator_service import IndicatorService

import pandas as pd
import numpy as np


app = FastAPI(
    title="Stock AI",
    version="1.0.0"
)


client = UpstoxClient()
collector = HistoricalDataCollector(client)
indicators = IndicatorService()


@app.get("/")
def health():

    return {
        "status": "running"
    }


@app.get("/financial/{isin}")
def financial(isin: str):

    return client.get_financial_statement(isin)


@app.get("/balance/{isin}")
def balance(isin: str):

    return client.get_balance_sheet(isin)


@app.get("/cashflow/{isin}")
def cashflow(isin: str):

    return client.get_cash_flow(isin)


@app.get("/ratios/{isin}")
def ratios(isin: str):

    return client.get_key_ratios(isin)


@app.get("/holding/{isin}")
def holding(isin: str):

    return client.get_share_holding(isin)


@app.get("/candles/{instrument_key}")
def candles(
        instrument_key: str,
        from_date: str,
        to_date: str):

    return client.get_historical_candles(
        instrument_key=instrument_key,
        from_date=from_date,
        to_date=to_date
    )


@app.get("/historical/{instrument_key}")
def historical_data(instrument_key: str):

    # Fetch 5 years of OHLCV data
    df = collector.fetch_five_years(instrument_key)

    if df is None or df.empty:
        return {
            "message": "No historical data found",
            "data": []
        }


    # Calculate indicators
    df = indicators.calculate(df)


    # Replace NaN and Infinity values
    df = df.replace(
        [
            np.nan,
            np.inf,
            -np.inf
        ],
        None
    )


    # Convert dataframe to records
    records = df.to_dict(
        orient="records"
    )


    return records