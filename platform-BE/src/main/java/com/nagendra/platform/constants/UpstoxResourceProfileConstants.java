package com.nagendra.platform.constants;

import java.util.List;

public class UpstoxResourceProfileConstants {

  public static final String MARKET_QUOTE_URL = "/market-quote/quotes?instrument_key=";
  public static final String INCOME_STATEMENTS = "/fundamentals/{isin}/income-statement";
  public static final String KEY_RATIOS = "/fundamentals/{isin}/key-ratios";
  public static final String SHARE_HOLDINGS = "/fundamentals/{isin}/share-holdings";
  public static final String BALANCE_SHEET = "/fundamentals/{isin}/balance-sheet";
  private static final double[] WEEK_1 = {0.35, 0.25, 0.20, 0.12, 0.08};
  private static final double[] WEEK_2 = {0.175, 0.125, 0.10, 0.06, 0.04};
  private static final double[] WEEK_3 = {0.0875, 0.0625, 0.05, 0.03, 0.02};
  private static final double[] WEEK_4 = {0.04375, 0.03125, 0.025, 0.015, 0.01};
  public static final List<double[]> WEEK_WEIGHTS = List.of(WEEK_1, WEEK_2, WEEK_3, WEEK_4);

  private UpstoxResourceProfileConstants() {}
}
