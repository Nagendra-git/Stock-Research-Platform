package com.nagendra.platform.service.impl;

import com.nagendra.platform.dto.StockDetailsDto;
import com.nagendra.platform.models.Instrument;
import com.nagendra.platform.service.InstrumentService;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentServiceImpl implements InstrumentService {

  private final MongoTemplate mongoTemplate;

  @Override
  public Set<String> getInstrumentKeys(List<String> companyNames) {

    List<Criteria> criteriaList =
        companyNames.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(name -> !name.isEmpty())
            .map(name -> Criteria.where("name").regex(".*" + Pattern.quote(name) + ".*", "i"))
            .toList();

    Query query = new Query();

    if (!criteriaList.isEmpty()) {
      query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
    }
    List<Instrument> instruments = mongoTemplate.find(query, Instrument.class);

    return instruments.stream().map(Instrument::getInstrumentKey).collect(Collectors.toSet());
  }

  @Override
  public String getInstrumentKey(StockDetailsDto stockDetails) {
    Query query = new Query();

    // Match both criteria exactly (Implicit AND)
    query.addCriteria(
        Criteria.where("tradingsymbol")
            .is(stockDetails.getTradingSymbol())
            .and("exchange")
            .is(stockDetails.getExchange()));

    // Execute the search against the stocks collection
    List<Instrument> instruments = mongoTemplate.find(query, Instrument.class);
    return instruments.isEmpty() ? "" : instruments.get(0).getInstrumentKey();
  }
}
