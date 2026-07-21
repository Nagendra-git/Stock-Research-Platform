package com.nagendra.platform.client;

import com.nagendra.platform.dto.StockRequestDto;
import com.nagendra.platform.dto.client.InstrumentData;
import com.nagendra.platform.dto.client.InstrumentResponse;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpstockClient {

  private final RestTemplate restTemplate;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Value("${upstox.api.access-token}")
  private String accessToken;

  @Value("${upstox.api.url}")
  private String baseUrl;

  public InstrumentResponse getStocksInfo(List<StockRequestDto> stocks) {

    List<CompletableFuture<InstrumentResponse>> futures =
        stocks.stream()
            .map(
                stock ->
                    CompletableFuture.supplyAsync(
                        () -> searchInstrument(stock.getTradingSymbol(), stock.getExchange()),
                        executorService))
            .toList();

    List<InstrumentData> allInstruments =
        futures.stream()
            .map(CompletableFuture::join)
            .filter(response -> response != null && response.getData() != null)
            .flatMap(response -> response.getData().stream())
            .toList();

    InstrumentResponse combined = new InstrumentResponse();
    combined.setStatus("success");
    combined.setData(allInstruments);
    return combined;
  }

  private InstrumentResponse searchInstrument(String query, String exchange) {

    String url =
        UriComponentsBuilder.fromUriString(baseUrl)
            .path("/instruments/search")
            .queryParam("query", query)
            .queryParam("segments", "EQ")
            .queryParam("exchanges", exchange)
            .queryParam("page_number", 1)
            .queryParam("records", 20)
            .toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setBearerAuth(accessToken);

    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    return restTemplate
        .exchange(url, HttpMethod.GET, requestEntity, InstrumentResponse.class)
        .getBody();
  }

  /**
   * Fetches detailed quote info for the given instrument keys.
   *
   * @param instrumentKeys e.g. ["NSE_EQ|INE002A01018", "NSE_EQ|INE009A01021"]
   * @return parsed response containing OHLC, depth, and quote data keyed by instrument
   */
  public MarketQuoteResponse getStocksDetailedInfo(List<String> instrumentKeys) {
    if (instrumentKeys == null || instrumentKeys.isEmpty()) {
      throw new IllegalArgumentException("instrumentKeys must not be null or empty");
    }

    String joinedKeys = String.join(",", instrumentKeys);

    String url = baseUrl + "/market-quote/quotes?instrument_key=" + joinedKeys;

    log.info(url);

    HttpEntity<Void> requestEntity = buildRequestEntity();

    try {
      return restTemplate
          .exchange(url, HttpMethod.GET, requestEntity, MarketQuoteResponse.class)
          .getBody();
    } catch (RestClientException e) {
      throw new IllegalArgumentException("Failed to fetch market quotes from Upstox", e);
    }
  }

  private HttpEntity<Void> buildRequestEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setBearerAuth(accessToken);
    return new HttpEntity<>(headers);
  }
}
