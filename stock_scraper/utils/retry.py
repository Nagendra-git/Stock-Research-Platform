import functools
import time

from requests.exceptions import RequestException

from utils.logger import logger


def retry(
    retries: int = 3,
    delay: int = 2,
    backoff: int = 2,
    exceptions=(RequestException,)
):
    """
    Retry decorator.

    retries  -> maximum attempts

    delay    -> initial wait time

    backoff  -> exponential multiplier
    """

    def decorator(func):

        @functools.wraps(func)
        def wrapper(*args, **kwargs):

            current_delay = delay

            for attempt in range(1, retries + 1):

                try:
                    return func(*args, **kwargs)

                except exceptions as ex:

                    logger.warning(
                        f"{func.__name__} failed "
                        f"(attempt {attempt}/{retries}) "
                        f"{str(ex)}"
                    )

                    if attempt == retries:
                        logger.error(
                            f"{func.__name__} failed permanently."
                        )
                        raise

                    time.sleep(current_delay)

                    current_delay *= backoff

        return wrapper

    return decorator