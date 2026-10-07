package com.nagendra.platform.repository;

import com.nagendra.platform.enums.PositionStatus;
import com.nagendra.platform.models.Position;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends MongoRepository<Position, String> {
  List<Position> findByStatus(PositionStatus status);

  List<Position> findByInstrumentKeyAndStatus(String instrumentKey, PositionStatus status);

  long countByStatus(PositionStatus status);
}
