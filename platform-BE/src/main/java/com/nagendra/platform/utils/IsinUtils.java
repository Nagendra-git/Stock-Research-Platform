package com.nagendra.platform.utils;

public final class IsinUtils {

  private IsinUtils() {}

  /**
   * Returns the actual ISIN. Examples: NSE_EQ|INE009A01021 -> INE009A01021 BSE_EQ|INE009A01021 ->
   * INE009A01021 INE009A01021 -> INE009A01021
   */
  public static String extractIsin(String instrumentKey) {
    if (instrumentKey == null || instrumentKey.isBlank()) {
      return null;
    }

    int index = instrumentKey.indexOf('|');
    return index >= 0 ? instrumentKey.substring(index + 1) : instrumentKey;
  }

  public static boolean isNse(String instrumentKey) {
    return instrumentKey != null && instrumentKey.startsWith("NSE_EQ|");
  }

  public static boolean isBse(String instrumentKey) {
    return instrumentKey != null && instrumentKey.startsWith("BSE_EQ|");
  }
}
