package com.nagendra.platform.service;

import com.nagendra.platform.dto.AddStockRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface StockService {
    void addStocks(AddStockRequestDto requestDto);
}
