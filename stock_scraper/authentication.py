from config import Config


class Authentication:

    @staticmethod
    def get_headers():

        return {

            "Accept": "application/json",

            "Authorization": f"Bearer {Config.ACCESS_TOKEN}"

        }