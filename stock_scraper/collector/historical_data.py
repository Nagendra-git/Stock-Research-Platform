import logging
from datetime import datetime, timedelta

import pandas as pd

from api.upstox_client import UpstoxClient

logger = logging.getLogger(__name__)


class HistoricalDataCollector:

    def __init__(self, client: UpstoxClient):
        self.client = client

    def fetch_five_years(self, instrument_key: str) -> pd.DataFrame:
        """
        Fetch last 5 years of DAILY candles.
        Returns a DataFrame sorted in ascending date order.
        """

        end_date = datetime.today().date()
        start_date = end_date - timedelta(days=365 * 5)

        all_candles = []

        current_to = end_date

        while current_to > start_date:

            current_from = max(
                start_date,
                current_to - timedelta(days=364)
            )

            logger.info(
                "Fetching candles %s -> %s",
                current_from,
                current_to
            )

            response = self.client.get_historical_candles(
                instrument_key=instrument_key,
                interval="days",
                unit=1,
                from_date=current_from.strftime("%Y-%m-%d"),
                to_date=current_to.strftime("%Y-%m-%d")
            )

            candles = response["data"]["candles"]

            if candles:
                all_candles.extend(candles)

            current_to = current_from - timedelta(days=1)

        if not all_candles:
            raise Exception("No historical candles found.")

        return self._convert_to_dataframe(all_candles)

    def _convert_to_dataframe(self, candles):

        df = pd.DataFrame(
            candles,
            columns=[
                "date",
                "open",
                "high",
                "low",
                "close",
                "volume",
                "oi"
            ]
        )

        df["date"] = pd.to_datetime(df["date"])
        df.sort_values("date", inplace=True)
        df.drop_duplicates(subset=["date"], inplace=True)
        df.reset_index(drop=True, inplace=True)

        return df