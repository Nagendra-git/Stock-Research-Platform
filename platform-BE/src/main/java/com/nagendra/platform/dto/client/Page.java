package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Page {

  @JsonProperty("page_number")
  private Integer pageNumber;

  @JsonProperty("total_pages")
  private Integer totalPages;

  private Integer records;

  @JsonProperty("total_records")
  private Integer totalRecords;
}
