package com.nagendra.platform.client;

import com.nagendra.platform.dto.client.InstrumentData;
import com.nagendra.platform.dto.client.InstrumentResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class UpstockClient {

  private final RestTemplate restTemplate;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Value("${upstox.api.access-token}")
  private String accessToken;

  @Value("${upstox.api.url}")
  private String baseUrl;

  public InstrumentResponse getStocksInfo(List<String> names) {

    List<CompletableFuture<InstrumentResponse>> futures =
        names.stream()
            .map(
                name ->
                    CompletableFuture.supplyAsync(() -> searchInstrument(name), executorService))
            .toList();

    List<InstrumentData> allInstruments =
        futures.stream()
            .map(CompletableFuture::join)
            .filter(response -> response != null && response.getData() != null)
            .flatMap(response -> response.getData().stream())
            .collect(
                Collectors.toMap(
                    InstrumentData::getUnderlyingKey,
                    instrument -> instrument,
                    (existing, replacement) -> existing,
                    LinkedHashMap::new))
            .values()
            .stream()
            .toList();

    InstrumentResponse combined = new InstrumentResponse();
    combined.setStatus("success");
    combined.setData(allInstruments);
    return combined;
  }

  private InstrumentResponse searchInstrument(String query) {

    String url =
        UriComponentsBuilder.fromUriString(baseUrl)
            .path("/instruments/search")
            .queryParam("query", query)
            .queryParam("segments", "EQ")
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
}
