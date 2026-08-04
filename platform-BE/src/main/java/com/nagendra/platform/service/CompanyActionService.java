package com.nagendra.platform.service;

public interface CompanyActionService {

  void addQuarterlyStocks();

  void getFundamentalScoreByIsIn(String isin);

  void getPriceScore(String isin);
}
