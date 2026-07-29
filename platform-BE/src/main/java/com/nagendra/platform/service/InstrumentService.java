package com.nagendra.platform.service;

import com.nagendra.platform.dto.StockDetailsDto;
import com.nagendra.platform.models.Instrument;
import java.util.List;
import java.util.Set;

public interface InstrumentService {
  Set<String> getInstrumentKeys(List<String> companyNames);

  String getInstrumentKey(StockDetailsDto stockDetails);

  List<Instrument> getAllInstruments(Double lp, Double up);

  void saveAll(List<Instrument> instruments);
}
