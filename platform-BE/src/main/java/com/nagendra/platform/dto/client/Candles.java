package com.nagendra.platform.dto.client;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Candles {

        private LocalDate date;

        private double open;

        private double high;

        private double low;

        private double close;

        private long volume;

        private long openInterest;
}
