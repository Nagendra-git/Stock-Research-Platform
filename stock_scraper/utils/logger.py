import logging
import os
from logging.handlers import RotatingFileHandler

LOG_DIRECTORY = "logs"
LOG_FILE = "stock_ai.log"

os.makedirs(LOG_DIRECTORY, exist_ok=True)

logger = logging.getLogger("StockAI")

logger.setLevel(logging.INFO)

formatter = logging.Formatter(
    "%(asctime)s | %(levelname)s | %(name)s | %(message)s"
)

console_handler = logging.StreamHandler()
console_handler.setFormatter(formatter)

file_handler = RotatingFileHandler(
    filename=os.path.join(LOG_DIRECTORY, LOG_FILE),
    maxBytes=5 * 1024 * 1024,
    backupCount=5,
    encoding="utf-8"
)
file_handler.setFormatter(formatter)

if not logger.handlers:
    logger.addHandler(console_handler)
    logger.addHandler(file_handler)