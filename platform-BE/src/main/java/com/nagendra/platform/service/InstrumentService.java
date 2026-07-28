package com.nagendra.platform.service;

import com.nagendra.platform.dto.StockDetailsDto;
import java.util.List;
import java.util.Set;

public interface InstrumentService {
  Set<String> getInstrumentKeys(List<String> companyNames);

  String getInstrumentKey(StockDetailsDto stockDetails);
}
