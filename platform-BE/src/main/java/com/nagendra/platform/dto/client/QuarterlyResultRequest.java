package com.nagendra.platform.dto.client;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarterlyResultRequest {

    private String fromDate;
    private String toDate;
    private String purpose;
    private List<String> type;
    private Integer pageNo;
    private Integer pageSize;
}