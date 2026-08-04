package com.nagendra.platform.client;

import com.nagendra.platform.constants.*;
import com.nagendra.platform.dto.client.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpstockClient {

  private final RestTemplate restTemplate;

  @Value("${upstox.api.access-token}")
  private String accessToken;

  @Value("${upstox.api.url}")
  private String baseUrl;

  /**
   * Fetches detailed quote info for the given instrument keys.
   *
   * @param instrumentKeys e.g. ["NSE_EQ|INE002A01018", "NSE_EQ|INE009A01021"]
   * @return parsed response containing OHLC, depth, and quote data keyed by instrument
   */
  public MarketQuoteResponse getStocksDetailedInfo(Set<String> instrumentKeys) {
    if (instrumentKeys == null || instrumentKeys.isEmpty()) {
      throw new IllegalArgumentException("instrumentKeys must not be null or empty");
    }

    String joinedKeys = String.join(",", instrumentKeys);

    String url = baseUrl + UpstoxResourceProfileConstants.MARKET_QUOTE_URL + joinedKeys;

    HttpEntity<Void> requestEntity = buildCommonRequestHeaders();

    try {
      return restTemplate
          .exchange(url, HttpMethod.GET, requestEntity, MarketQuoteResponse.class)
          .getBody();
    } catch (RestClientException e) {
      throw new IllegalArgumentException("Failed to fetch market quotes from Upstox", e);
    }
  }

  public FinancialStatementResponse getFinancialStatements(String isin) {

    String url =
        UriComponentsBuilder.fromUriString(
                baseUrl + UpstoxResourceProfileConstants.INCOME_STATEMENTS)
            .queryParam("type", "standalone")
            .queryParam("time_period", "quarterly")
            .queryParam("fs", true)
            .buildAndExpand(isin)
            .toUriString();

    HttpEntity<Void> request = buildCommonRequestHeaders();

    ResponseEntity<FinancialStatementResponse> response =
        restTemplate.exchange(url, HttpMethod.GET, request, FinancialStatementResponse.class);

    return response.getBody();
  }

  public HttpEntity<Void> buildCommonRequestHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setBearerAuth(accessToken);
    return new HttpEntity<>(headers);
  }

  public KeyRatiosResponse getKeyRatios(String isin) {

    String url =
        UriComponentsBuilder.fromUriString(baseUrl + UpstoxResourceProfileConstants.KEY_RATIOS)
            .buildAndExpand(isin)
            .toUriString();

    HttpEntity<Void> requestEntity = buildCommonRequestHeaders();

    ResponseEntity<KeyRatiosResponse> response =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, KeyRatiosResponse.class);

    return response.getBody();
  }

  public ShareHoldingResponse getHoldingData(String isin) {

    String url =
        UriComponentsBuilder.fromUriString(baseUrl + UpstoxResourceProfileConstants.SHARE_HOLDINGS)
            .buildAndExpand(isin)
            .toUriString();

    HttpEntity<Void> requestEntity = buildCommonRequestHeaders();

    ResponseEntity<ShareHoldingResponse> response =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, ShareHoldingResponse.class);

    return response.getBody();
  }

  public BalanceSheetResponse getBalanceSheet(String isin) {

    String url =
        UriComponentsBuilder.fromUriString(baseUrl + UpstoxResourceProfileConstants.BALANCE_SHEET)
            .queryParam("type", "standalone")
            .queryParam("fs", true)
            .buildAndExpand(isin)
            .toUriString();

    HttpEntity<Void> requestEntity = buildCommonRequestHeaders();

    ResponseEntity<BalanceSheetResponse> response =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, BalanceSheetResponse.class);

    return response.getBody();
  }

  public PriceHistoryResponse getPriceHistory(String isin) {

    try {
      String url = buildUrl(isin);
      HttpEntity<Void> requestEntity = buildCommonRequestHeaders();

      ResponseEntity<PriceHistoryResponse> response =
          restTemplate.exchange(url, HttpMethod.GET, requestEntity, PriceHistoryResponse.class);

      return response.getBody();

    } catch (Exception e) {
      log.info("Error fetching price history for ISIN {}", isin);
    }
    return null;
  }

  private String buildUrl(String instrumentKey) {
    LocalDate toDate = LocalDate.now();
    LocalDate fromDate = toDate.minusMonths(7);

    return String.format(
        "https://api.upstox.com/v3/historical-candle/%s/days/1/%s/%s",
        instrumentKey, toDate, fromDate);
  }
}
