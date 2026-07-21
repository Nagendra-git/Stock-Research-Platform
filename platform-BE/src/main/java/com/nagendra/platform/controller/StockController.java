package com.nagendra.platform.controller;
import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ResponseEntity<String> upsertStocks(@RequestBody AddStockRequestDto requestDto){
        stockService.addStocks(requestDto);
        return new ResponseEntity<>("Successfully added stocks", HttpStatus.OK);
    }
}
