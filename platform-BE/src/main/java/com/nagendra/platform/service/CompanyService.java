package com.nagendra.platform.service;

import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.models.Company;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface CompanyService {

  MarketQuoteResponse getStockInfo();

  List<Company> saveAll(List<Company> companies);
}
