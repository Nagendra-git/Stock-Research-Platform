import apiClient from "./apiClient";
import type { WeeklyMomentum } from "../../types/WeeklyMomentum"

export interface WeeklyMomentumResponse {
  content: WeeklyMomentum[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export async function getWeeklyMomentum(
  page: number,
  size: number,
  sortBy: string,
  direction: string
): Promise<WeeklyMomentumResponse> {
  const response = await apiClient.get<WeeklyMomentumResponse>(
    "/weekly-momentum/all",
    {
      params: { page, size, sortBy, direction },
    }
  );

  return response.data;
}
