import pandas as pd
import pandas_ta as ta


class IndicatorService:
    """
    Calculates technical indicators from historical candle data.
    """

    def calculate(self, candle_df: pd.DataFrame) -> pd.DataFrame:

        if candle_df is None or candle_df.empty:
            raise Exception("Historical candle data is empty.")

        df = candle_df.copy()

        # ---------------------------------------------------
        # Clean & Prepare Data
        # ---------------------------------------------------

        df["date"] = pd.to_datetime(df["date"])

        df.sort_values("date", inplace=True)

        df.drop_duplicates(
            subset=["date"],
            inplace=True
        )

        df.reset_index(
            drop=True,
            inplace=True
        )


        # Convert numeric columns
        numeric_columns = [
            "open",
            "high",
            "low",
            "close",
            "volume"
        ]

        for col in numeric_columns:
            df[col] = pd.to_numeric(
                df[col],
                errors="coerce"
            )


        df.dropna(
            subset=["close"],
            inplace=True
        )


        # VWAP requires datetime index
        df.set_index(
            "date",
            inplace=True
        )


        # ---------------------------------------------------
        # EMA
        # ---------------------------------------------------

        df["ema20"] = ta.ema(
            df["close"],
            length=20
        )

        df["ema50"] = ta.ema(
            df["close"],
            length=50
        )

        df["ema100"] = ta.ema(
            df["close"],
            length=100
        )

        df["ema200"] = ta.ema(
            df["close"],
            length=200
        )


        # Convert None -> NaN
        for col in [
            "ema20",
            "ema50",
            "ema100",
            "ema200"
        ]:
            df[col] = pd.to_numeric(
                df[col],
                errors="coerce"
            )


        # ---------------------------------------------------
        # EMA Trend
        # ---------------------------------------------------

        df["ema_trend"] = 0


        valid_ema = (
            df["ema50"].notna() &
            df["ema200"].notna()
        )


        df.loc[
            valid_ema &
            (df["ema50"] > df["ema200"]),
            "ema_trend"
        ] = 1


        df.loc[
            valid_ema &
            (df["ema50"] < df["ema200"]),
            "ema_trend"
        ] = -1



        # ---------------------------------------------------
        # RSI
        # ---------------------------------------------------

        df["rsi14"] = ta.rsi(
            df["close"],
            length=14
        )



        # ---------------------------------------------------
        # MACD
        # ---------------------------------------------------

        macd = ta.macd(
            df["close"]
        )

        if macd is not None:

            df["macd"] = macd.iloc[:, 0]

            df["macd_signal"] = macd.iloc[:, 1]

            df["macd_histogram"] = macd.iloc[:, 2]



        # ---------------------------------------------------
        # ATR
        # ---------------------------------------------------

        df["atr14"] = ta.atr(
            high=df["high"],
            low=df["low"],
            close=df["close"],
            length=14
        )



        # ---------------------------------------------------
        # ADX
        # ---------------------------------------------------

        adx = ta.adx(
            high=df["high"],
            low=df["low"],
            close=df["close"],
            length=14
        )


        if adx is not None:

            df["adx"] = adx.iloc[:, 0]

            df["+di"] = adx.iloc[:, 1]

            df["-di"] = adx.iloc[:, 2]



        # ---------------------------------------------------
        # Bollinger Bands
        # ---------------------------------------------------

        bb = ta.bbands(
            df["close"],
            length=20
        )


        if bb is not None:

            df["bb_lower"] = bb.iloc[:, 0]

            df["bb_middle"] = bb.iloc[:, 1]

            df["bb_upper"] = bb.iloc[:, 2]

            df["bb_bandwidth"] = bb.iloc[:, 3]

            df["bb_percent"] = bb.iloc[:, 4]



        # ---------------------------------------------------
        # VWAP
        # ---------------------------------------------------

        df["vwap"] = ta.vwap(
            high=df["high"],
            low=df["low"],
            close=df["close"],
            volume=df["volume"]
        )



        # ---------------------------------------------------
        # Volume
        # ---------------------------------------------------

        df["volume_sma20"] = ta.sma(
            df["volume"],
            length=20
        )


        df["volume_ratio"] = (
            df["volume"] /
            df["volume_sma20"]
        )



        # ---------------------------------------------------
        # Returns
        # ---------------------------------------------------

        df["return_1d"] = (
            df["close"]
            .pct_change(1)
        )

        df["return_5d"] = (
            df["close"]
            .pct_change(5)
        )

        df["return_20d"] = (
            df["close"]
            .pct_change(20)
        )

        df["return_60d"] = (
            df["close"]
            .pct_change(60)
        )



        # ---------------------------------------------------
        # Volatility
        # ---------------------------------------------------

        df["volatility20"] = (
            df["return_1d"]
            .rolling(20)
            .std()
        )



        # ---------------------------------------------------
        # EMA Distance
        # ---------------------------------------------------

        df["ema20_distance"] = (
            (df["close"] - df["ema20"])
            /
            df["ema20"]
        )


        df["ema50_distance"] = (
            (df["close"] - df["ema50"])
            /
            df["ema50"]
        )


        df["ema200_distance"] = (
            (df["close"] - df["ema200"])
            /
            df["ema200"]
        )



        # ---------------------------------------------------
        # 52 Week High / Low
        # ---------------------------------------------------

        df["52_week_high"] = (
            df["high"]
            .rolling(252)
            .max()
        )


        df["52_week_low"] = (
            df["low"]
            .rolling(252)
            .min()
        )


        df["distance_from_high"] = (
            (df["close"] - df["52_week_high"])
            /
            df["52_week_high"]
        )


        df["distance_from_low"] = (
            (df["close"] - df["52_week_low"])
            /
            df["52_week_low"]
        )



        # ---------------------------------------------------
        # Golden Cross / Death Cross
        # ---------------------------------------------------

        df["golden_cross"] = (
            df["ema50"].notna() &
            df["ema200"].notna() &
            (df["ema50"] > df["ema200"])
        ).astype(int)


        df["death_cross"] = (
            df["ema50"].notna() &
            df["ema200"].notna() &
            (df["ema50"] < df["ema200"])
        ).astype(int)



        # ---------------------------------------------------
        # Trend
        # ---------------------------------------------------

        df["trend"] = 0


        valid_trend = (
            df["ema20"].notna() &
            df["ema50"].notna() &
            df["ema200"].notna()
        )


        df.loc[
            valid_trend &
            (df["ema20"] > df["ema50"]) &
            (df["ema50"] > df["ema200"]),
            "trend"
        ] = 1


        df.loc[
            valid_trend &
            (df["ema20"] < df["ema50"]) &
            (df["ema50"] < df["ema200"]),
            "trend"
        ] = -1



        # ---------------------------------------------------
        # Price Above EMA200
        # ---------------------------------------------------

        df["above_ema200"] = (
            df["ema200"].notna() &
            (df["close"] > df["ema200"])
        ).astype(int)



        # ---------------------------------------------------
        # Remove only invalid rows
        # ---------------------------------------------------

        df.dropna(
            subset=["close"],
            inplace=True
        )


        # Restore date column

        df.reset_index(
            inplace=True
        )


        return df