from dotenv import load_dotenv
import os

load_dotenv()


class Config:

    BASE_URL = os.getenv("UPSTOX_BASE_URL")

    ACCESS_TOKEN = os.getenv("UPSTOX_ACCESS_TOKEN")

    MODEL_PATH = os.getenv("MODEL_PATH")

    DATASET_PATH = os.getenv("DATASET_PATH")

    LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")