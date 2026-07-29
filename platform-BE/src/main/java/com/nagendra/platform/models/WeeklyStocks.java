package com.nagendra.platform.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document("weekly_stocks")
@EqualsAndHashCode(callSuper = false)
public class WeeklyStocks extends Audit{

    @Id
    private String id;

    private String isin;

    private BigDecimal stockPrice;

    private String symbol;

    private Double weeklyScore;

    private Boolean isMomentumScore;

    private Boolean isFundamentalScore;

    private Double expectedQuarterlyResults;
}
