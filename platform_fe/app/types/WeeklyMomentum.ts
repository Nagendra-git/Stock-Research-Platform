export interface WeeklyMomentum {
  id: string;

  symbol: string;
  companyName: string;

  swingScore: number;

  buyerSellerScore: number;
  monthlyBuyerSellerScore: number;
  threeMonthBuyerSellerScore: number;
  sixMonthBuyerSellerScore: number;

  weeklyPricePercentage: number;
  monthlyPricePercentage: number;
  threeMonthPricePercentage: number;
  sixMonthPricePercentage: number;

  weeklyVolumePercentage: number;
  monthlyVolumePercentage: number;
  threeMonthVolumePercentage: number;
  sixMonthVolumePercentage: number;

  weeklyTurnoverPercentage: number;
  monthlyTurnoverPercentage: number;
  threeMonthTurnoverPercentage: number;
  sixMonthTurnoverPercentage: number;

  pastThreeDaysPricePercentage: number;

  tradingCategories: string[];
}