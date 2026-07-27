package com.nagendra.platform.repository;

import com.nagendra.platform.models.MomentumScore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MomentumScoreRepository extends MongoRepository<MomentumScore, String> {
  MomentumScore findByIsin(String s);
}
