import requests

from authentication import Authentication
from config import Config
from utils.retry import retry
from utils.logger import logger


class UpstoxClient:

    def __init__(self):

        self.base_url = Config.BASE_URL

        self.headers = Authentication.get_headers()

    @retry(retries=3)
    def get(self, endpoint, params=None):

        url = f"{self.base_url}{endpoint}"
        logger.info(f"GET {url}")
        response = requests.get(
            url=url,
            headers=self.headers,
            params=params,
            timeout=30
        )

        response.raise_for_status()

        return response.json()

    def get_financial_statement(self, isin):

        return self.get(
            f"/v2/fundamentals/{isin}/income-statement?type=consolidated&time_period=yearly&fs=true"
        )


    def get_balance_sheet(self, isin):

        return self.get(
            f"/v2/fundamentals/{isin}/balance-sheet?type=consolidated&fs=true"
        )


    def get_cash_flow(self, isin):

        return self.get(
            f"/v2/fundamentals/{isin}/cash-flow?type=consolidated&fs=true"
        )


    def get_key_ratios(self, isin):

        return self.get(
            f"/v2/fundamentals/{isin}/key-ratios"
        )


    def get_share_holding(self, isin):

        return self.get(
            f"/v2/fundamentals/{isin}/share-holdings"
        )


    def get_historical_candles(
        self,
        instrument_key: str,
        interval: str,
        unit: int,
        from_date: str,
        to_date: str
    ):
        endpoint = (
            f"/v3/historical-candle/"
            f"{instrument_key}/"
            f"{interval}/"
            f"{unit}/"
            f"{to_date}/"
            f"{from_date}"
        )

        return self.get(endpoint)

    