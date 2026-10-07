package com.nagendra.platform.service;

import com.nagendra.platform.dto.marketdata.CreatePositionRequest;
import com.nagendra.platform.dto.marketdata.PositionSummaryResponse;
import com.nagendra.platform.dto.position.PositionResult;
import com.nagendra.platform.enums.ExitReason;
import com.nagendra.platform.models.Position;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PositionService {

  void loadOpenPositionsIntoCache();

  List<Position> getOpenPositionsForInstrument(String instrumentKey);

  Optional<Position> exitPosition(String positionId, BigDecimal exitPrice, ExitReason reason);

  void persistTrailingStopUpdate(String positionId, BigDecimal newStopLoss, BigDecimal newHigh);

  Optional<Position> manualExit(String positionId);

  void squareOffAll(ExitReason reason);

  List<Position> listAll();

  List<Position> listOpen();

  Optional<Position> getById(String id);

  PositionSummaryResponse getSummary();

  PositionResult createPosition(CreatePositionRequest request);
}
