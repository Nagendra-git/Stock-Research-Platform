package com.nagendra.platform.repository;

import com.nagendra.platform.models.Instrument;
import java.util.List;
import java.util.Set;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentRepository extends MongoRepository<Instrument, String> {
  List<Instrument> findByIsinIn(Set<String> isins);

  List<Instrument> findByInstrumentKeyIn(Set<String> isins);
}
