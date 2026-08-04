import { useEffect, useState } from "react";
import type { WeeklyMomentum } from "../../types/WeeklyMomentum";

import { getWeeklyMomentum } from "../../services/weeklyService/weeklyService";
import type { WeeklyMomentumResponse } from "../../services/weeklyService/weeklyService";
import WeeklyTrendTable from "../../components/weekly/WeeklyTrendTable";
import "./WeeklyTrend.css";

export default function WeeklyTrendPage() {
  const [data, setData] = useState<WeeklyMomentum[]>([]);
  const [loading, setLoading] = useState(true);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [page, setPage] = useState(0);
  const size = 10;

  const [sortBy, setSortBy] = useState("swingScore");
  const [direction, setDirection] = useState("desc");
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, sortBy, direction]);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      const response: WeeklyMomentumResponse = await getWeeklyMomentum(
        page,
        size,
        sortBy,
        direction
      );

      setData(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      setError("Failed to load weekly trend data. Please try again.");
      console.error(err);
    } finally {
      setLoading(false);
      setIsInitialLoad(false);
    }
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

      {error && <p className="negative">{error}</p>}

      {/* Only show the full-page loading text on the very first load,
          when there's no table to keep mounted yet. */}
      {isInitialLoad && loading ? (
        <p>Loading...</p>
      ) : (
        <>
          <div
            className="table-loading-overlay"
            style={{ opacity: loading ? 0.5 : 1, transition: "opacity 0.15s ease" }}
          >
            <WeeklyTrendTable
              data={data}
              sortBy={sortBy}
              direction={direction}
              onSort={handleSort}
            />
          </div>

          <div className="pagination">
            <button
              disabled={page === 0 || loading}
              onClick={() => setPage(page - 1)}
            >
              Previous
            </button>

            <span>
              Page {page + 1} of {Math.max(totalPages, 1)}
            </span>

            <button
              disabled={page + 1 >= totalPages || loading}
              onClick={() => setPage(page + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}