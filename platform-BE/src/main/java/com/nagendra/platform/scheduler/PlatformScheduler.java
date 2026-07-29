package com.nagendra.platform.scheduler;

import com.nagendra.platform.models.Instrument;
import com.nagendra.platform.models.WeeklyStocks;
import com.nagendra.platform.service.InstrumentService;
import com.nagendra.platform.service.WeeklyMomentumService;
import com.nagendra.platform.service.WeeklyStockService;
import com.nagendra.platform.utils.IsinUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlatformScheduler {

  private final InstrumentService instrumentService;
  private final WeeklyMomentumService momentumService;
  private final WeeklyStockService weeklyStockService;

  public static <T> List<List<T>> partition(List<T> list, int size) {

    List<List<T>> result = new ArrayList<>();

    for (int i = 0; i < list.size(); i += size) {

      int end = Math.min(i + size, list.size());

      result.add(list.subList(i, end));
    }

    return result;
  }

  @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")
  public void fetchDailyCandles() {
    log.info("Scheduler started ......");
    weeklyStockService.removeAllStocks();
    log.info("Removed all stocks from weekly stocks collection");
    Double lp = 0.1;
    Double up = 100.0;
    List<Instrument> instruments = instrumentService.getAllInstruments(lp, up);

    // Remove BSE if NSE exists for same ISIN
    List<Instrument> filteredInstruments = removeBseDuplicates(instruments);
    log.info(
        "Filtered instruments size after removing BSE duplicates: {}", filteredInstruments.size());
    List<List<Instrument>> mainBatches = partition(filteredInstruments, 1000);

    processMainBatches(mainBatches);
  }

  private List<Instrument> removeBseDuplicates(List<Instrument> instruments) {

    Map<String, List<Instrument>> groupedByIsin =
        instruments.stream()
            .filter(instrument -> instrument.getInstrumentKey() != null)
            .collect(
                Collectors.groupingBy(
                    instrument -> IsinUtils.extractIsin(instrument.getInstrumentKey())));

    List<Instrument> instrumentsToRemove = new ArrayList<>();

    for (List<Instrument> instrumentList : groupedByIsin.values()) {

      if (instrumentList.size() <= 1) {
        continue;
      }

      boolean hasNse =
          instrumentList.stream()
              .anyMatch(instrument -> IsinUtils.isNse(instrument.getInstrumentKey()));

      boolean hasBse =
          instrumentList.stream()
              .anyMatch(instrument -> IsinUtils.isBse(instrument.getInstrumentKey()));

      // If both NSE and BSE exist, remove BSE
      if (hasNse && hasBse) {
        instrumentList.stream()
            .filter(instrument -> IsinUtils.isBse(instrument.getInstrumentKey()))
            .forEach(instrumentsToRemove::add);
      }
    }

    instruments.removeAll(instrumentsToRemove);

    return instruments;
  }

  private void processMainBatches(List<List<Instrument>> batches) {

    for (List<Instrument> batch : batches) {

      process1000Batch(batch);

      // wait 30 minutes
      sleepMinutes(30);
    }
  }

  private void sleepMinutes(int minutes) {
    try {
      Thread.sleep(Duration.ofMinutes(minutes).toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Thread interrupted during sleep", e);
    }
  }

  private void process1000Batch(List<Instrument> instruments) {

    List<List<Instrument>> smallBatches = partition(instruments, 100);

    for (List<Instrument> batch : smallBatches) {

      process100(batch);

      sleepMinutes(1);
    }
  }

  private void process100(List<Instrument> instruments) {
    momentumService.addWeeklyMomentum(instruments);
  }

  @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Kolkata")
  public void updateWeeklyScore() {
    log.info("Processing weekly stocks to update weekly momentum scores");
    List<WeeklyStocks> stocks = weeklyStockService.getAllStocks();
    processWeeklyStocksInBatches(stocks);
    log.info("Completed processing weekly stocks to update weekly momentum scores");
  }

  private void processWeeklyStocksInBatches(List<WeeklyStocks> stocks) {

    List<List<WeeklyStocks>> mainBatches = partition(stocks, 1000);

    for (int i = 0; i < mainBatches.size(); i++) {

      log.info("Starting main batch {}/{}", i + 1, mainBatches.size());

      processMainBatch(mainBatches.get(i));

      // Wait 30 minutes before next 1000 batch
      if (i < mainBatches.size() - 1) {
        sleepMinutes(30);
      }
    }
  }

  private void processMainBatch(List<WeeklyStocks> stocks) {

    List<List<WeeklyStocks>> subBatches = partition(stocks, 100);

    for (int i = 0; i < subBatches.size(); i++) {

      log.info("Processing sub batch {}/{}", i + 1, subBatches.size());

      for (WeeklyStocks stock : subBatches.get(i)) {
        try {
          momentumService.updateWeeklyTrendInMemory(stock);
        } catch (Exception e) {
          log.error("Failed to process {}", stock.getIsin());
        }
      }

      // Wait 1 minute before next 100-stock batch
      if (i < subBatches.size() - 1) {
        sleepMinutes(1);
      }
    }
  }
}
