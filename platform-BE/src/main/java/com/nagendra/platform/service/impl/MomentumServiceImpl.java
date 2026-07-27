package com.nagendra.platform.service.impl;

import com.nagendra.platform.models.MomentumScore;
import com.nagendra.platform.repository.MomentumScoreRepository;
import com.nagendra.platform.service.MomentumService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MomentumServiceImpl implements MomentumService {
  private final MomentumScoreRepository scoreRepository;

  @Override
  public void saveMomentumScore(MomentumScore momentumScore) {
    scoreRepository.save(momentumScore);
  }

  @Override
  public MomentumScore getMomentumScore(String s) {
    return scoreRepository.findByIsin(s);
  }

  @Override
  public List<MomentumScore> getMomentumScores() {
    return scoreRepository.findAll();
  }

  @Override
  public void deleteAll(List<MomentumScore> scoresToRemove) {
    scoreRepository.deleteAll(scoresToRemove);
  }
}
