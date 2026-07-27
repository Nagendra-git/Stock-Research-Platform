package com.nagendra.platform.filters;

import com.nagendra.platform.dto.client.Candles;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VolumeScore {

    public double calculateVolumeScore(
            List<Candles> candles) {

        if (candles.size() < 21) {
            return 0;
        }

        candles.sort(
                Comparator.comparing(Candles::getDate));

        Candles latest =
                candles.get(candles.size() - 1);

        long currentVolume =
                latest.getVolume();

        List<Candles> previous20Days =
                candles.subList(
                        candles.size() - 21,
                        candles.size() - 1);

        double averageVolume =
                previous20Days.stream()
                        .mapToLong(Candles::getVolume)
                        .average()
                        .orElse(0);


        double ratio =
                currentVolume / averageVolume;



        return calculateVolumeScore(ratio);
    }

    private int calculateVolumeScore(double ratio) {
        if (ratio >= 3.0) {
            return 15;
        } else if (ratio >= 2.0) {
            return 10;
        } else if (ratio >= 1.0) {
            return 5;
        }
        return 0;
    }
}
