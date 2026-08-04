"use client";

import { useEffect, useState } from "react";
import { WeeklyMomentum } from "../types/WeeklyMomentum";
import {
  getWeeklyMomentum,
  WeeklyMomentumResponse,
} from "../services/weeklyService";
import WeeklyTrendTable from "../components/weekly/WeeklyTrendTable";

export default function WeeklyTrendPage() {
  const [data, setData] = useState<WeeklyMomentum[]>([]);

  const [page, setPage] = useState(0);
  const size = 10;

  const [sortBy, setSortBy] = useState("swingScore");
  const [direction, setDirection] = useState("desc");
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadData();
  }, [page, sortBy, direction]);

  async function loadData() {
    const response: WeeklyMomentumResponse = await getWeeklyMomentum(
      page,
      size,
      sortBy,
      direction
    );

    setData(response.content);
    setTotalPages(response.totalPages);
  }

  function handleSort(field: string) {
    if (field === sortBy) {
      setDirection(direction === "asc" ? "desc" : "asc");
    } else {
      setSortBy(field);
      setDirection("desc");
    }
    setPage(0);
  }

  return (
    <div className="weekly-container">
      <WeeklyTrendTable
        data={data}
        sortBy={sortBy}
        direction={direction}
        onSort={handleSort}
      />

      <div className="pagination">
        <button
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
        >
          Previous
        </button>

        <span>
          Page {page + 1} of {totalPages}
        </span>

        <button
          disabled={page + 1 >= totalPages}
          onClick={() => setPage(page + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}