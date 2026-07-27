package com.nagendra.platform.repository;

import com.nagendra.platform.models.Instrument;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentRepository extends MongoRepository<Instrument, String> {}
