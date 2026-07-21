package com.nagendra.platform.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum StockCategory {

    MY_INVESTMENT,
    RESEARCH,
    TOP_GAINERS,
    TOP_LOSERS,
    QUARTERLY_PERFORMERS,
    HIGH_GROWTH,
    VALUE_STOCKS;

    @JsonCreator
    public static StockCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Stock category must not be empty");
        }
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value.trim().replace(" ", "_")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid stock category: '" + value + "'. Allowed values: " +
                                Arrays.toString(values())));
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}