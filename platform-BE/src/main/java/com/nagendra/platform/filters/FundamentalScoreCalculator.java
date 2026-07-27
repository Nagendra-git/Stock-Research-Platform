package com.nagendra.platform.filters;

import com.nagendra.platform.dto.filters.FundamentalScoreRequestDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class FundamentalScoreCalculator {



  public Integer getFundamentalScoreCalculator(FundamentalScoreRequestDto dto) {

    int salesScore =
        dto.getRevenueGrowthPercentage() != null
            ? calculateSalesGrowthScore(dto.getRevenueGrowthPercentage())
            : 0;

    int profitScore =
        dto.getProfitGrowthPercentage() != null
            ? calculateProfitGrowthScore(dto.getProfitGrowthPercentage())
            : 0;

    int roceScore = dto.getRoce() != null ? calculateRoceScore(dto.getRoce()) : 0;

    int institutionalScore =
        dto.getCurrentFii() != null
                && dto.getPreviousFii() != null
                && dto.getCurrentDii() != null
                && dto.getPreviousDii() != null
            ? calculateInstitutionalScore(
                dto.getCurrentFii(),
                dto.getPreviousFii(),
                dto.getCurrentDii(),
                dto.getPreviousDii())
            : 0;

    int financialHealthScore =
        dto.getCurrentAssets() != null
                && dto.getPreviousAssets() != null
                && dto.getCurrentLiabilities() != null
                && dto.getPreviousLiabilities() != null
            ? calculateFinancialHealthScore(
                dto.getCurrentAssets(),
                dto.getPreviousAssets(),
                dto.getCurrentLiabilities(),
                dto.getPreviousLiabilities())
            : 0;

    return salesScore + profitScore + roceScore + institutionalScore + financialHealthScore;
  }

  /** Sales Growth Score */
  public int calculateSalesGrowthScore(Double growth) {
    if (growth == null || growth == 0) return 0;
    if (growth > 30) return 5;
    if (growth > 20) return 4;
    if (growth > 10) return 3;
    if (growth > 0) return 2;

    return 0;
  }

  /** Profit Growth Score */
  public int calculateProfitGrowthScore(Double growth) {
    if (growth == null || growth == 0) return 0;
    if (growth > 30) return 5;
    if (growth > 20) return 4;
    if (growth > 10) return 3;
    if (growth > 0) return 2;

    return 0;
  }

  /** ROCE Score */
  public int calculateRoceScore(double roce) {

    if (roce > 25) return 5;
    if (roce > 20) return 4;
    if (roce > 15) return 3;
    if (roce > 10) return 2;

    return 0;
  }

  /** FII / DII Score */
  public int calculateInstitutionalScore(
      double currentFii, double previousFii, double currentDii, double previousDii) {

    boolean fiiIncreasing = currentFii > previousFii;
    boolean diiIncreasing = currentDii > previousDii;

    if (fiiIncreasing && diiIncreasing) return 5;

    if (fiiIncreasing) return 4;

    if (diiIncreasing) return 3;

    if (currentFii == previousFii && currentDii == previousDii) return 2;

    return 0;
  }

  /** Balance Sheet Health */
  public int calculateFinancialHealthScore(
      BigDecimal currentAssets,
      BigDecimal previousAssets,
      BigDecimal currentLiabilities,
      BigDecimal previousLiabilities) {

    double assetGrowth = percentageGrowth(currentAssets, previousAssets);
    double liabilityGrowth = percentageGrowth(currentLiabilities, previousLiabilities);

    // Assets increasing while liabilities decreasing
    if (assetGrowth > 10 && liabilityGrowth < 0) return 5;

    // Assets growing significantly faster
    if (assetGrowth - liabilityGrowth >= 15) return 4;

    // Assets growing slightly faster
    if (assetGrowth > liabilityGrowth) return 3;

    // Assets still growing but liabilities growing faster
    if (assetGrowth > 0) return 2;

    // Assets shrinking
    return 0;
  }

  /** Generic Growth Calculator */
  private double percentageGrowth(BigDecimal current, BigDecimal previous) {

    if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
      return 0;
    }

    return current
        .subtract(previous)
        .divide(previous, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
  }
  
}
