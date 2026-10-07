package com.nagendra.platform.service.impl;

import com.nagendra.platform.service.MarketInstrumentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarketInstrumentServiceImpl implements MarketInstrumentService {

  @Override
  public List<String> getInstrumentKeys() {
    return List.of("BSE_EQ|INE531X01018", "BSE_EQ|INE013J01016", "NSE_EQ|INE0I0M01023");
  }
}
