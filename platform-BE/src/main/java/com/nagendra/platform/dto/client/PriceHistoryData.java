package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nagendra.platform.utils.CandlesDeserializer;
import java.util.List;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;

@Data
public class PriceHistoryData {

    @JsonProperty("candles")
    @JsonDeserialize(contentUsing = CandlesDeserializer.class)
    private List<Candles> candles;
}
