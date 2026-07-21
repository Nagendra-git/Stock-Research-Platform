package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class InstrumentResponse {

    private String status;

    private List<InstrumentData> data;

    @JsonProperty("meta_data")
    private MetaData metaData;
}