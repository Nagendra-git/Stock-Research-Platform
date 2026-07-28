package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.SoldStockDto;
import com.nagendra.platform.dto.StackStatisticsDto;
import com.nagendra.platform.dto.StockStatistics;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.dto.client.Quote;
import com.nagendra.platform.enums.StockCategory;
import com.nagendra.platform.models.Stock;
import com.nagendra.platform.models.StockCategoryMapping;
import com.nagendra.platform.repository.StockRepository;
import com.nagendra.platform.service.InstrumentService;
import com.nagendra.platform.service.StockCategoryMappingService;
import com.nagendra.platform.service.StockService;
import com.nagendra.platform.service.StockStatisticsService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    private final StockCategoryMappingService mappingService;

    private final InstrumentService instrumentService;
    private final UpstockClient upstockClient;

    private final StockStatisticsService stockStatisticsService;
    
    @Override
    @Transactional
    public void addStocks(AddStockRequestDto requestDto) {

        String instrumentKey = instrumentService.getInstrumentKey(requestDto.getStockDetails());
        MarketQuoteResponse response = upstockClient.getStocksDetailedInfo(Set.of(instrumentKey));
        Map<String, Quote> quoteMap =
                response.getData().values().stream()
                        .collect(Collectors.toMap(Quote::getInstrumentToken, quote -> quote));
        Quote quote = quoteMap.get(instrumentKey);
        Stock stock = new Stock();
        stock.setIsin(instrumentKey);
        stock.setStockPrice(BigDecimal.valueOf(quote.getLastPrice()));
        stock.setBoughtPrice(requestDto.getStockDetails().getBoughtPrice());
        stock.setQuantity(requestDto.getStockDetails().getQuantity());
        stock.setSymbol(quote.getSymbol());
        Stock result = stockRepository.save(stock);
        StockCategory stockCategory = StockCategory.fromString(requestDto.getStockCategory());
        StockCategoryMapping mapping = new StockCategoryMapping();
        mapping.setCategory(stockCategory);
        mapping.setStockId(result.getId());
        mappingService.saveMapping(mapping);
    }

    @Override
    @Transactional
    public void deleteStock(String id) {
        mappingService.removeStockCategory(id);
        stockRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStockData(String stockId, SoldStockDto soldStockDto) {
        Stock stock = getById(stockId);
        stock.setSoldPrice(soldStockDto.getSoldPrice());
        stock.setQuantity(soldStockDto.getQuantity());
        stockRepository.save(stock);
    }

    @Override
    public List<Stock> getMyInvestmentStocks() {
        return stockRepository.findAll();
    }
    @Override
    public StockStatistics calculateStats(String stockId) {
        Stock stock = getById(stockId);
        StackStatisticsDto dto = buildStackStatisticsDto(stock);
        return stockStatisticsService.calculateStats(dto);
    }

    private StackStatisticsDto buildStackStatisticsDto(Stock stock) {
        return StackStatisticsDto.builder()
                .boughtPrice(stock.getBoughtPrice())
                .soldPrice(stock.getSoldPrice())
                .currentPrice(stock.getStockPrice())
                .quantity(stock.getQuantity())
                .build();
    }

    private Stock getById(String stockId) {
        return stockRepository.findById(stockId).orElseThrow();
    }
}
