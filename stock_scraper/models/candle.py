from dataclasses import dataclass
from datetime import datetime


@dataclass
class Candle:

    timestamp: datetime

    open: float

    high: float

    low: float

    close: float

    volume: int

    open_interest: int = 0

    def to_dict(self):

        return {

            "Date": self.timestamp,

            "Open": self.open,

            "High": self.high,

            "Low": self.low,

            "Close": self.close,

            "Volume": self.volume,

            "OpenInterest": self.open_interest

        }