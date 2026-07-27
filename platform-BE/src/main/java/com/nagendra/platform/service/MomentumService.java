package com.nagendra.platform.service;

import com.nagendra.platform.models.MomentumScore;
import java.util.List;

public interface MomentumService {
  void saveMomentumScore(MomentumScore momentumScore);

  MomentumScore getMomentumScore(String s);

  List<MomentumScore> getMomentumScores();

  void deleteAll(List<MomentumScore> scoresToRemove);
}
