package com.nagendra.platform.service.impl;

import com.nagendra.platform.config.TradingProperties;
import com.nagendra.platform.dto.marketdata.*;
import com.nagendra.platform.dto.order.OrderRequest;
import com.nagendra.platform.dto.order.OrderResult;
import com.nagendra.platform.dto.position.PositionResult;
import com.nagendra.platform.enums.*;
import com.nagendra.platform.exception.PositionException;
import com.nagendra.platform.models.Position;
import com.nagendra.platform.repository.PositionRepository;
import com.nagendra.platform.service.MarketDataRedisService;
import com.nagendra.platform.service.OrderExecutionService;
import com.nagendra.platform.service.PositionService;
import com.nagendra.platform.trading.PositionSizingService;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Owns the full lifecycle of a Position: sizing + entry, and exit (both manual and triggered by
 * PositionMonitorService). Also keeps the in-memory "which positions are open for instrument X"
 * cache that PositionMonitorService reads on every tick - a Mongo query per tick per instrument
 * would be far too slow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PositionServiceImpl implements PositionService {

  private final PositionRepository repository;
  private final PositionSizingService sizingService;
  private final OrderExecutionService orderExecutionService;
  private final MarketDataRedisService marketDataRedisService;
  private final TradingProperties properties;

  private final Map<String, CopyOnWriteArrayList<Position>> openByInstrument =
      new ConcurrentHashMap<>();

  /**
   * Rebuild the open-position cache from Mongo on startup/restart so monitoring survives a
   * redeploy.
   */
  @Override
  @PostConstruct
  public void loadOpenPositionsIntoCache() {
    List<Position> open = repository.findByStatus(PositionStatus.OPEN);
    for (Position p : open) {
      openByInstrument
          .computeIfAbsent(p.getInstrumentKey(), k -> new CopyOnWriteArrayList<>())
          .add(p);
    }
    log.info("Loaded {} open position(s) into monitor cache on startup", open.size());
  }

  @Override
  public List<Position> getOpenPositionsForInstrument(String instrumentKey) {
    return openByInstrument.getOrDefault(instrumentKey, new CopyOnWriteArrayList<>());
  }

  public synchronized PositionResult createPosition(CreatePositionRequest request) {
    long openCount = repository.countByStatus(PositionStatus.OPEN);
    if (openCount >= properties.getMaxOpenPositions()) {
      return PositionResult.rejected(
          "Max open positions reached (" + properties.getMaxOpenPositions() + ")");
    }

    BigDecimal referencePrice =
        marketDataRedisService.getCurrentPrice(request.getInstrumentKey()).orElse(null);
    if (referencePrice == null && request.getOrderType() == OrderType.MARKET) {
      return PositionResult.rejected(
          "No live price yet for "
              + request.getInstrumentKey()
              + " - feed may not be connected/subscribed");
    }

    BigDecimal expectedEntryPrice =
        request.getOrderType() == OrderType.LIMIT ? request.getLimitPrice() : referencePrice;
    if (expectedEntryPrice == null || expectedEntryPrice.signum() <= 0) {
      return PositionResult.rejected("Could not determine an entry price");
    }

    BigDecimal stopLossPrice = resolveStopLoss(request, expectedEntryPrice);
    BigDecimal targetPrice = resolveTarget(request, expectedEntryPrice);
    if (stopLossPrice.compareTo(expectedEntryPrice) >= 0) {
      return PositionResult.rejected("Stop-loss must be below entry price for a long position");
    }
    if (targetPrice.compareTo(expectedEntryPrice) <= 0) {
      return PositionResult.rejected("Target must be above entry price for a long position");
    }

    int quantity;
    BigDecimal capitalAllocated;
    if (request.getQuantity() != null && request.getQuantity() > 0) {
      quantity = request.getQuantity();
      capitalAllocated = expectedEntryPrice.multiply(BigDecimal.valueOf(quantity));
    } else {
      capitalAllocated = resolveCapitalAllocated(request);
      PositionSizingService.SizingResult sizing =
          sizingService.calculateQuantity(capitalAllocated, expectedEntryPrice, stopLossPrice);
      if (!sizing.isOk()) {
        return PositionResult.rejected(sizing.getMessage());
      }
      quantity = sizing.getQuantity();
    }

    String product =
        request.getProduct() != null ? request.getProduct() : properties.getDefaultProduct();

    OrderRequest orderRequest =
        new OrderRequest(
            request.getInstrumentKey(),
            quantity,
            TransactionType.BUY,
            request.getOrderType(),
            request.getOrderType() == OrderType.LIMIT ? request.getLimitPrice() : null,
            referencePrice,
            product,
            "entry");
    OrderResult orderResult = orderExecutionService.placeOrder(orderRequest);
    if (!orderResult.isAccepted()) {
      return PositionResult.rejected("Entry order rejected: " + orderResult.getMessage());
    }

    Position position = new Position();
    position.setInstrumentKey(request.getInstrumentKey());
    position.setSymbol(request.getSymbol());
    position.setStatus(PositionStatus.OPEN);
    position.setQuantity(quantity);
    position.setCapitalAllocated(capitalAllocated);
    position.setRequestedEntryPrice(
        request.getOrderType() == OrderType.LIMIT ? request.getLimitPrice() : null);
    position.setEntryPrice(
        orderResult.getFillPrice() != null ? orderResult.getFillPrice() : expectedEntryPrice);
    position.setEntryOrderId(orderResult.getOrderId());
    position.setEntryTime(LocalDateTime.now());
    position.setStopLossPrice(stopLossPrice);
    position.setTargetPrice(targetPrice);
    position.setHighestPriceSinceEntry(position.getEntryPrice());
    position.setProduct(product);
    if (request.getTrailingStopPercent() != null && request.getTrailingStopPercent().signum() > 0) {
      position.setTrailingStopEnabled(true);
      position.setTrailingStopPercent(request.getTrailingStopPercent());
    }

    position = repository.save(position);
    openByInstrument
        .computeIfAbsent(position.getInstrumentKey(), k -> new CopyOnWriteArrayList<>())
        .add(position);

    log.info(
        "Opened position {} on {}: qty={} entry={} sl={} target={}",
        position.getId(),
        position.getInstrumentKey(),
        quantity,
        position.getEntryPrice(),
        stopLossPrice,
        targetPrice);
    return PositionResult.accepted(position);
  }

  /**
   * Called by PositionMonitorService when a tick crosses a position's stop-loss/target, and by the
   * manual-exit REST endpoint. Idempotent in effect: if the position is no longer OPEN (already
   * exited by a concurrent call), this is a no-op.
   */
  @Override
  public synchronized Optional<Position> exitPosition(
      String positionId, BigDecimal exitPrice, ExitReason reason) {
    Position position = repository.findById(positionId).orElse(null);
    if (position == null || position.getStatus() != PositionStatus.OPEN) {
      return Optional.empty(); // already exited or never opened - avoid double-exit
    }

    OrderRequest orderRequest =
        new OrderRequest(
            position.getInstrumentKey(),
            position.getQuantity(),
            TransactionType.SELL,
            OrderType.MARKET,
            null,
            exitPrice,
            position.getProduct(),
            "exit-" + reason);
    OrderResult orderResult = orderExecutionService.placeOrder(orderRequest);

    if (!orderResult.isAccepted()) {
      log.error(
          "Exit order for position {} was REJECTED - position remains OPEN and will be retried on the "
              + "next tick: {}",
          position.getId(),
          orderResult.getMessage());
      return Optional.empty();
    }

    BigDecimal fillPrice =
        orderResult.getFillPrice() != null ? orderResult.getFillPrice() : exitPrice;
    BigDecimal pnl =
        fillPrice
            .subtract(position.getEntryPrice())
            .multiply(BigDecimal.valueOf(position.getQuantity()))
            .setScale(2, RoundingMode.HALF_UP);

    position.setStatus(PositionStatus.EXITED);
    position.setExitPrice(fillPrice);
    position.setExitOrderId(orderResult.getOrderId());
    position.setExitTime(LocalDateTime.now());
    position.setExitReason(reason);
    position.setRealizedPnl(pnl);
    repository.save(position);

    CopyOnWriteArrayList<Position> instrumentPositions =
        openByInstrument.get(position.getInstrumentKey());
    if (instrumentPositions != null) {
      instrumentPositions.removeIf(p -> p.getId().equals(position.getId()));
    }

    log.info(
        "Closed position {} on {}: reason={} exitPrice={} pnl={}",
        position.getId(),
        position.getInstrumentKey(),
        reason,
        fillPrice,
        pnl);
    return Optional.of(position);
  }

  /**
   * Persists a ratcheted trailing-stop move. Called off the WS feed thread by
   * PositionMonitorService.
   */
  @Override
  public synchronized void persistTrailingStopUpdate(
      String positionId, BigDecimal newStopLoss, BigDecimal newHigh) {
    repository
        .findById(positionId)
        .ifPresent(
            p -> {
              if (p.getStatus() != PositionStatus.OPEN) {
                return; // exited between the trigger and this write - nothing to persist
              }
              p.setStopLossPrice(newStopLoss);
              p.setHighestPriceSinceEntry(newHigh);
              repository.save(p);
            });
  }

  @Override
  public Optional<Position> manualExit(String positionId) {
    Position position =
        repository
            .findById(positionId)
            .orElseThrow(() -> new PositionException("Position not found: " + positionId));
    BigDecimal ltp =
        marketDataRedisService
            .getCurrentPrice(position.getInstrumentKey())
            .orElseThrow(
                () ->
                    new PositionException(
                        "No live price available to exit " + position.getInstrumentKey()));
    return exitPosition(positionId, ltp, ExitReason.MANUAL);
  }

  /** Force-exits every OPEN position, e.g. for an end-of-day square-off. */
  public void squareOffAll(ExitReason reason) {
    for (Position position : repository.findByStatus(PositionStatus.OPEN)) {
      BigDecimal ltp =
          marketDataRedisService.getCurrentPrice(position.getInstrumentKey()).orElse(null);
      if (ltp == null) {
        log.warn(
            "Cannot square off position {} - no live price for {}",
            position.getId(),
            position.getInstrumentKey());
        continue;
      }
      exitPosition(position.getId(), ltp, reason);
    }
  }

  @Override
  public List<Position> listAll() {
    return repository.findAll();
  }

  @Override
  public List<Position> listOpen() {
    return repository.findByStatus(PositionStatus.OPEN);
  }

  @Override
  public Optional<Position> getById(String id) {
    return repository.findById(id);
  }

  private BigDecimal resolveStopLoss(CreatePositionRequest request, BigDecimal entryPrice) {
    if (request.getStopLossPrice() != null) {
      return request.getStopLossPrice();
    }
    BigDecimal pct =
        request.getStopLossPercent() != null
            ? request.getStopLossPercent()
            : properties.getDefaultStopLossPercent();
    return entryPrice
        .multiply(
            BigDecimal.ONE.subtract(pct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal resolveTarget(CreatePositionRequest request, BigDecimal entryPrice) {
    if (request.getTargetPrice() != null) {
      return request.getTargetPrice();
    }
    BigDecimal pct =
        request.getTargetPercent() != null
            ? request.getTargetPercent()
            : properties.getDefaultTargetPercent();
    return entryPrice
        .multiply(BigDecimal.ONE.add(pct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal resolveCapitalAllocated(CreatePositionRequest request) {
    if (request.getCapitalAllocated() != null) {
      return request.getCapitalAllocated();
    }
    return properties
        .getTotalCapital()
        .multiply(properties.getMaxCapitalPerTradePercent())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
  }

  @Override
  public PositionSummaryResponse getSummary() {

    List<Position> positions = repository.findAll();

    BigDecimal investment = BigDecimal.ZERO;

    BigDecimal bookedPnl = BigDecimal.ZERO;

    BigDecimal holdingPnl = BigDecimal.ZERO;

    BigDecimal currentValue = BigDecimal.ZERO;

    long gainStocks = 0;
    long lossStocks = 0;
    long holdingStocks = 0;
    long soldStocks = 0;

    List<PositionGainLoss> stocks = new ArrayList<>();

    for (Position position : positions) {

      BigDecimal capital =
          position.getCapitalAllocated() != null ? position.getCapitalAllocated() : BigDecimal.ZERO;

      investment = investment.add(capital);

      BigDecimal pnl = BigDecimal.ZERO;

      /*
       * CLOSED POSITION
       */
      if (position.getStatus() == PositionStatus.EXITED) {

        soldStocks++;

        if (position.getRealizedPnl() != null) {
          pnl = position.getRealizedPnl();
        }

        bookedPnl = bookedPnl.add(pnl);

      }

      /*
       * OPEN POSITION
       */
      else if (position.getStatus() == PositionStatus.OPEN) {

        holdingStocks++;

        /*
         * IMPORTANT:
         * Get current price from your Redis market-data service.
         */
        BigDecimal currentPrice =
            marketDataRedisService
                .getCurrentPrice(position.getInstrumentKey())
                .orElse(position.getEntryPrice());

        if (currentPrice != null && position.getEntryPrice() != null) {

          pnl =
              currentPrice
                  .subtract(position.getEntryPrice())
                  .multiply(BigDecimal.valueOf(position.getQuantity()));

          currentValue =
              currentValue.add(currentPrice.multiply(BigDecimal.valueOf(position.getQuantity())));
        }

        holdingPnl = holdingPnl.add(pnl);
      }

      /*
       * STOCK LEVEL STATISTICS
       */
      if (pnl.compareTo(BigDecimal.ZERO) > 0) {
        gainStocks++;
      } else if (pnl.compareTo(BigDecimal.ZERO) < 0) {
        lossStocks++;
      }

      BigDecimal percentage = BigDecimal.ZERO;

      if (capital.compareTo(BigDecimal.ZERO) > 0) {

        percentage =
            pnl.abs().multiply(BigDecimal.valueOf(100)).divide(capital, 2, RoundingMode.HALF_UP);
      }

      PnlType type = pnl.compareTo(BigDecimal.ZERO) >= 0 ? PnlType.GAIN : PnlType.LOSS;

      stocks.add(
          PositionGainLoss.builder()
              .symbol(position.getSymbol())
              .gainLoss(
                  PnlSummary.builder().amount(pnl.abs()).percentage(percentage).type(type).build())
              .build());
    }

    /*
     * OVERALL
     */
    BigDecimal overallPnl = bookedPnl.add(holdingPnl);

    BigDecimal overallPercentage = BigDecimal.ZERO;

    if (investment.compareTo(BigDecimal.ZERO) > 0) {

      overallPercentage =
          overallPnl
              .abs()
              .multiply(BigDecimal.valueOf(100))
              .divide(investment, 2, RoundingMode.HALF_UP);
    }

    /*
     * BOOKED %
     */
    BigDecimal bookedPercentage = BigDecimal.ZERO;

    if (investment.compareTo(BigDecimal.ZERO) > 0) {

      bookedPercentage =
          bookedPnl
              .abs()
              .multiply(BigDecimal.valueOf(100))
              .divide(investment, 2, RoundingMode.HALF_UP);
    }

    /*
     * HOLDING %
     */
    BigDecimal holdingPercentage = BigDecimal.ZERO;

    if (investment.compareTo(BigDecimal.ZERO) > 0) {

      holdingPercentage =
          holdingPnl
              .abs()
              .multiply(BigDecimal.valueOf(100))
              .divide(investment, 2, RoundingMode.HALF_UP);
    }

    return PositionSummaryResponse.builder()
        .portfolioSummary(
            PortfolioSummary.builder()
                .investment(investment)
                .currentValue(currentValue.setScale(2, RoundingMode.HALF_UP))
                .booked(buildPnlSummary(bookedPnl, bookedPercentage))
                .holding(buildPnlSummary(holdingPnl, holdingPercentage))
                .overall(buildPnlSummary(overallPnl, overallPercentage))
                .build())
        .statistics(
            PositionStatistics.builder()
                .gainStocks(gainStocks)
                .lossStocks(lossStocks)
                .holdingStocks(holdingStocks)
                .soldStocks(soldStocks)
                .totalStocks(positions.size())
                .build())
        .stocks(stocks)
        .build();
  }

  private PnlSummary buildPnlSummary(BigDecimal pnl, BigDecimal percentage) {

    PnlType type = pnl.compareTo(BigDecimal.ZERO) >= 0 ? PnlType.GAIN : PnlType.LOSS;

    return PnlSummary.builder().amount(pnl.abs()).percentage(percentage).type(type).build();
  }
}
