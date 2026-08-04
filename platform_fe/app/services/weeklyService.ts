import axios from "axios";
import { WeeklyMomentum } from "../types/WeeklyMomentum";

const API = `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/weekly-momentum/all`;

export interface WeeklyMomentumResponse {
    content: WeeklyMomentum[];
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
    first: boolean;
    last: boolean;
}

export async function getWeeklyMomentum(
    page: number,
    size: number,
    sortBy: string,
    direction: string
) {
    const response = await axios.get<WeeklyMomentumResponse>(API, {
        params: {
            page,
            size,
            sortBy,
            direction,
        },
    });

    return response.data;
}