package com.nagendra.platform.service;

import java.util.List;
import java.util.Set;

public interface InstrumentService {
  Set<String> getInstrumentKeys(List<String> companyNames);
}
