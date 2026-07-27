package com.nagendra.platform.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommonUtils {

  public static String extractIsin(String instrumentKey) {

    if (instrumentKey == null || instrumentKey.isBlank()) {
      return null;
    }

    String value = instrumentKey.trim();

    // If instrument key contains exchange prefix
    int index = value.indexOf('|');

    if (index != -1 && index < value.length() - 1) {
      return value.substring(index + 1).trim();
    }

    // Already an ISIN
    return value;
  }
}
