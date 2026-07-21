package com.nagendra.platform.dto;

import java.util.List;
import lombok.Data;

@Data
public class AddStockRequestDto {
    private List<String> names;
    private String stockCategory;
}
