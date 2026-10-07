package com.nagendra.platform.upstox;

import com.nagendra.platform.dto.marketdata.MarketData;
import com.upstox.marketdatafeeder.rpc.proto.Feed;
import com.upstox.marketdatafeeder.rpc.proto.FeedResponse;
import com.upstox.marketdatafeeder.rpc.proto.FullFeed;
import com.upstox.marketdatafeeder.rpc.proto.IndexFullFeed;
import com.upstox.marketdatafeeder.rpc.proto.LTPC;
import com.upstox.marketdatafeeder.rpc.proto.MarketFullFeed;
import com.upstox.marketdatafeeder.rpc.proto.OptionGreeks;
import com.upstox.marketdatafeeder.rpc.proto.Quote;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Converts a parsed FeedResponse into instrumentKey -> MarketData.
 *
 * <p>Handles the real V3 nesting: feed -> fullFeed -> marketFF (equities/derivatives) | indexFF
 * (indices) feed -> ltpc (ltpc-only mode) feed -> firstLevelWithGreeks (option_greeks mode)
 *
 * <p>Every nested section is optional and is checked via the oneof "case" before being accessed -
 * never assumes optionGreeks/marketLevel exist.
 */
@Component
public class ProtoFeedMapper {

  private static final Logger log = LoggerFactory.getLogger(ProtoFeedMapper.class);

  public Map<String, MarketData> map(FeedResponse response) {
    Map<String, MarketData> result = new HashMap<>();
    long currentTs = response.getCurrentTs();

    response
        .getFeedsMap()
        .forEach(
            (instrumentKey, feed) -> {
              try {
                Optional<MarketData> md = mapFeed(instrumentKey, feed, currentTs);
                md.ifPresent(m -> result.put(instrumentKey, m));
              } catch (Exception e) {
                // A single malformed/unexpected feed entry must never
                // take down the whole batch.
                log.warn(
                    "Failed to parse feed for instrument {}: {}", instrumentKey, e.getMessage());
              }
            });
    return result;
  }

  private Optional<MarketData> mapFeed(String instrumentKey, Feed feed, long currentTs) {
    MarketData md = new MarketData();
    md.setInstrumentKey(instrumentKey);
    md.setCurrentTimestamp(currentTs);
    md.setUpdatedAt(LocalDateTime.now());

    switch (feed.getFeedUnionCase()) {
      case LTPC -> applyLtpc(md, feed.getLtpc());
      case FULLFEED -> applyFullFeed(md, feed.getFullFeed());
      case FIRSTLEVELWITHGREEKS -> {
        var flg = feed.getFirstLevelWithGreeks();
        applyLtpc(md, flg.getLtpc());
        if (flg.hasFirstDepth()) {
          applyQuote(md, flg.getFirstDepth());
        }
        if (flg.hasOptionGreeks()) {
          applyGreeks(md, flg.getOptionGreeks());
        }
        md.setVolumeTradedToday(flg.getVtt());
        md.setOpenInterest(flg.getOi());
        md.setImpliedVolatility(bd(flg.getIv()));
      }
      default -> {
        log.debug(
            "Feed for {} has no populated union (heartbeat/empty tick) - skipping", instrumentKey);
        return Optional.empty();
      }
    }
    return Optional.of(md);
  }

  private void applyFullFeed(MarketData md, FullFeed fullFeed) {
    switch (fullFeed.getFullFeedUnionCase()) {
      case MARKETFF -> applyMarketFullFeed(md, fullFeed.getMarketFF());
      case INDEXFF -> applyIndexFullFeed(md, fullFeed.getIndexFF());
      default ->
          log.debug(
              "FullFeed has neither marketFF nor indexFF populated for {}", md.getInstrumentKey());
    }
  }

  private void applyMarketFullFeed(MarketData md, MarketFullFeed marketFF) {
    if (marketFF.hasLtpc()) {
      applyLtpc(md, marketFF.getLtpc());
    }
    if (marketFF.hasMarketLevel() && marketFF.getMarketLevel().getBidAskQuoteCount() > 0) {
      // Best (first) depth level
      applyQuote(md, marketFF.getMarketLevel().getBidAskQuote(0));
    }
    if (marketFF.hasOptionGreeks()) {
      applyGreeks(md, marketFF.getOptionGreeks());
    }
    md.setAverageTradedPrice(bd(marketFF.getAtp()));
    md.setVolumeTradedToday(marketFF.getVtt());
    md.setOpenInterest(marketFF.getOi());
    md.setImpliedVolatility(bd(marketFF.getIv()));
    md.setTotalBuyQuantity(marketFF.getTbq());
    md.setTotalSellQuantity(marketFF.getTsq());
  }

  private void applyIndexFullFeed(MarketData md, IndexFullFeed indexFF) {
    if (indexFF.hasLtpc()) {
      applyLtpc(md, indexFF.getLtpc());
    }
    // Indices don't carry bid/ask, greeks, OI, etc.
  }

  private void applyLtpc(MarketData md, LTPC ltpc) {
    md.setLtp(bd(ltpc.getLtp()));
    md.setLastTradedTime(ltpc.getLtt());
    md.setLastTradedQuantity(ltpc.getLtq());
    md.setClosePrice(bd(ltpc.getCp()));
  }

  private void applyQuote(MarketData md, Quote quote) {
    md.setBidPrice(bd(quote.getBidP()));
    md.setBidQuantity(quote.getBidQ());
    md.setAskPrice(bd(quote.getAskP()));
    md.setAskQuantity(quote.getAskQ());
  }

  private void applyGreeks(MarketData md, OptionGreeks greeks) {
    md.setDelta(bd(greeks.getDelta()));
    md.setTheta(bd(greeks.getTheta()));
    md.setGamma(bd(greeks.getGamma()));
    md.setVega(bd(greeks.getVega()));
    md.setRho(bd(greeks.getRho()));
  }

  private BigDecimal bd(double value) {
    return BigDecimal.valueOf(value);
  }
}
