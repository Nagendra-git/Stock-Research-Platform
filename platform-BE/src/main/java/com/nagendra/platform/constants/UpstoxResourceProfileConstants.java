package com.nagendra.platform.constants;

public class UpstoxResourceProfileConstants {

  public static final String MARKET_QUOTE_URL = "/market-quote/quotes?instrument_key=";

  public static final String INCOME_STATEMENTS = "/fundamentals/{isin}/income-statement";

  public static final String KEY_RATIOS = "/fundamentals/{isin}/key-ratios";

  public static final String SHARE_HOLDINGS = "/fundamentals/{isin}/share-holdings";

  public static final String BALANCE_SHEET = "/fundamentals/{isin}/balance-sheet";

  private UpstoxResourceProfileConstants() {}
}
