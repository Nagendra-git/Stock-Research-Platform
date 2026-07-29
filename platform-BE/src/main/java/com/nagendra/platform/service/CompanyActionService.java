package com.nagendra.platform.service;

public interface CompanyActionService {

  // TODO need to write schedular to run every data
  void addQuarterlyStocks();

  void getFundamentalScoreByIsIn(String isin);

  void getPriceScore(String isin);

  void getShortTermScore(String isin);
}
