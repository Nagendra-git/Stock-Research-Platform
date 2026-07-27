package com.nagendra.platform.client;

import com.nagendra.platform.dto.client.BoardMeetings;
import com.nagendra.platform.dto.client.CorporateActionResponse;
import com.nagendra.platform.dto.client.QuarterlyResultRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class LivemintClient {

  private static final String URL =
      "https://api-mintgenie.livemint.com/api-gateway/fundamental/api/getCorporateActionDionData";
  private final RestTemplate restTemplate;

  public List<BoardMeetings> fetchResults(LocalDate fromDate, LocalDate toDate) {

    int page = 0;
    int pageSize = 50;

    List<BoardMeetings> allResults = new ArrayList<>();

    while (true) {

      QuarterlyResultRequest request =
          QuarterlyResultRequest.builder()
              .fromDate(fromDate.toString())
              .toDate(toDate.toString())
              .purpose("result")
              .type(List.of("BoardMeeting"))
              .pageNo(page)
              .pageSize(pageSize)
              .build();

      ResponseEntity<CorporateActionResponse> response =
          restTemplate.exchange(
              URL,
              HttpMethod.POST,
              new HttpEntity<>(request, createHeaders()),
              CorporateActionResponse.class);
      List<BoardMeetings> records;
      if (response.getBody() != null) {
        records = response.getBody().getBoardMeetings();
      } else {
        return new ArrayList<>();
      }

      if (records == null || records.isEmpty()) {
        break;
      }

      allResults.addAll(records);

      if (records.size() < pageSize) {
        break;
      }

      page++;
    }

    return allResults;
  }

  private HttpHeaders createHeaders() {

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    headers.set("accept", "*/*");
    headers.set("mintgenie-client", "LM-WEB");
    headers.set("origin", "https://www.livemint.com");
    headers.set("referer", "https://www.livemint.com/market/quarterly-results-calendar");
    headers.set("User-Agent", "Mozilla/5.0");

    return headers;
  }
}
