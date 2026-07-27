"use client";

import React, { useEffect, useMemo, useState, useCallback } from "react";
import {
  Search,
  Bell,
  MessageSquare,
  ChevronDown,
  LayoutGrid,
  Bitcoin,
  TrendingUp,
  TrendingDown,
} from "lucide-react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";

/**
 * ------------------------------------------------------------------
 * API CONTRACT (Spring Boot)
 * ------------------------------------------------------------------
 * Base URL is read from VITE_API_BASE_URL (or NEXT_PUBLIC_API_BASE_URL,
 * adjust getApiBase() below to match your build tool). All endpoints are
 * expected to be behind your existing auth (cookie / bearer token) —
 * add an Authorization header in `authFetch` if you use JWTs.
 *
 * GET  /api/investments/overview
 *   -> {
 *        totalValue: number,
 *        todayChangePercent: number,
 *        todayChangeAmount: number,
 *        monthlyReturnPercent: number,
 *        monthlyReturnAmount: number,
 *        allocations: { label: string, percent: number, value: number }[]
 *      }
 *
 * GET  /api/investments/growth?range=1D|1M|6M|1Y
 *   -> {
 *        range: string,
 *        points: { label: string, value: number }[],
 *        highlight: {
 *          label: string,
 *          value: number,
 *          changePercent: number,
 *          changeAmount: number,
 *          monthlyReturnPercent: number,
 *          depositsAdded: number
 *        } | null
 *      }
 *
 * GET  /api/watchlist
 *   -> {
 *        symbol: string,
 *        name: string,
 *        price: number,
 *        changePercent: number,
 *        marketCap: string,
 *        iconUrl?: string
 *      }[]
 *
 * GET  /api/credit-card/overview
 *   -> {
 *        cardholderName: string,
 *        maskedNumber: string,   // e.g. "1253 5432 3521 3990"
 *        expiry: string,         // e.g. "09/26"
 *        cvvMasked: string,      // e.g. "•••"
 *        balanceAvailable: number,
 *        creditLimit: number
 *      }
 *
 * GET  /api/me
 *   -> { firstName: string, handle: string, avatarUrl: string }
 * ------------------------------------------------------------------
 */

type Allocation = { label: string; percent: number; value: number };

type Overview = {
  totalValue: number;
  todayChangePercent: number;
  todayChangeAmount: number;
  monthlyReturnPercent: number;
  monthlyReturnAmount: number;
  allocations: Allocation[];
};

type GrowthPoint = { label: string; value: number };

type GrowthHighlight = {
  label: string;
  value: number;
  changePercent: number;
  changeAmount: number;
  monthlyReturnPercent: number;
  depositsAdded: number;
};

type GrowthData = {
  range: string;
  points: GrowthPoint[];
  highlight: GrowthHighlight | null;
};

type WatchlistItem = {
  symbol: string;
  name: string;
  price: number;
  changePercent: number;
  marketCap: string;
  iconUrl?: string;
};

type CreditCardOverview = {
  cardholderName: string;
  maskedNumber: string;
  expiry: string;
  cvvMasked: string;
  balanceAvailable: number;
  creditLimit: number;
};

type Profile = { firstName: string; handle: string; avatarUrl: string };

type Range = "1D" | "1M" | "6M" | "1Y";

const RANGES: Range[] = ["1D", "1M", "6M", "1Y"];

function getApiBase(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL || "/api";
}

async function authFetch<T>(path: string): Promise<T> {
  const res = await fetch(`${getApiBase()}${path}`, {
    credentials: "include",
    headers: {
      Accept: "application/json",
      // Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) {
    throw new Error(`Request to ${path} failed with status ${res.status}`);
  }
  return res.json() as Promise<T>;
}

function formatCurrency(value: number, maximumFractionDigits = 2): string {
  return value.toLocaleString("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits,
  });
}

function formatCompactCurrency(value: number): string {
  return "$" + Math.round(value).toLocaleString("en-US");
}

function formatSignedPercent(value: number): string {
  const sign = value > 0 ? "+" : "";
  return `${sign}${value.toFixed(2)}%`;
}

const ALLOCATION_COLORS = ["#7C6CF6", "#4F8EF7", "#F5B94D", "#4FD1A5"];

/* ------------------------------- Skeletons ------------------------------- */

function CardSkeleton({ className = "" }: { className?: string }) {
  return (
    <div className={`animate-pulse rounded-2xl bg-white/5 ${className}`} />
  );
}

/* --------------------------------- Chart --------------------------------- */

function GrowthChart({ data }: { data: GrowthData }) {
  const chartData = data.points;
  const highlightLabel = data.highlight?.label;

  return (
    <ResponsiveContainer width="100%" height={280}>
      <AreaChart
        data={chartData}
        margin={{ top: 10, right: 10, left: -10, bottom: 0 }}
      >
        <defs>
          <linearGradient id="growthFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#8B7CF6" stopOpacity={0.35} />
            <stop offset="100%" stopColor="#8B7CF6" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid vertical={false} stroke="rgba(255,255,255,0.06)" />
        <XAxis
          dataKey="label"
          axisLine={false}
          tickLine={false}
          tick={{ fill: "rgba(255,255,255,0.45)", fontSize: 12 }}
        />
        <YAxis
          axisLine={false}
          tickLine={false}
          tick={{ fill: "rgba(255,255,255,0.45)", fontSize: 12 }}
          tickFormatter={(v) => formatCompactCurrency(v).replace("$", "$") }
          width={56}
        />
        <Tooltip content={<GrowthTooltip highlight={data.highlight} />} />
        {highlightLabel && (
          <Area
            type="monotone"
            dataKey="value"
            stroke="#8B7CF6"
            strokeWidth={2.5}
            fill="url(#growthFill)"
            activeDot={{ r: 5, fill: "#8B7CF6", stroke: "#fff", strokeWidth: 2 }}
          />
        )}
        {!highlightLabel && (
          <Area
            type="monotone"
            dataKey="value"
            stroke="#8B7CF6"
            strokeWidth={2.5}
            fill="url(#growthFill)"
            activeDot={{ r: 5, fill: "#8B7CF6", stroke: "#fff", strokeWidth: 2 }}
          />
        )}
      </AreaChart>
    </ResponsiveContainer>
  );
}

function GrowthTooltip({
  active,
  payload,
  highlight,
}: {
  active?: boolean;
  payload?: any[];
  highlight: GrowthHighlight | null;
}) {
  if (!active || !payload || !payload.length) return null;
  const point = payload[0].payload as GrowthPoint;
  const h = highlight && highlight.label === point.label ? highlight : null;

  return (
    <div className="rounded-xl border border-white/10 bg-[#241c47] px-4 py-3 shadow-xl min-w-[190px]">
      <p className="text-[11px] uppercase tracking-wide text-white/50">
        Investment Value
      </p>
      <p className="text-lg font-semibold text-white">
        {formatCompactCurrency(point.value)}
      </p>
      {h && (
        <>
          <div className="mt-2 space-y-1">
            <p className="text-[11px] text-white/50">Change from Previous Month</p>
            <p className="text-sm font-medium text-emerald-400">
              {formatSignedPercent(h.changePercent)} (
              {formatCompactCurrency(h.changeAmount)})
            </p>
          </div>
          <div className="mt-2 space-y-1">
            <p className="text-[11px] text-white/50">Monthly Return</p>
            <p className="text-sm font-medium text-emerald-400">
              {formatSignedPercent(h.monthlyReturnPercent)}
            </p>
          </div>
          <div className="mt-2 space-y-1">
            <p className="text-[11px] text-white/50">Deposits Added</p>
            <p className="text-sm font-medium text-white">
              {formatCompactCurrency(h.depositsAdded)}
            </p>
          </div>
        </>
      )}
    </div>
  );
}

/* -------------------------------- Page ----------------------------------- */

export default function InvestmentsPage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [overview, setOverview] = useState<Overview | null>(null);
  const [growth, setGrowth] = useState<GrowthData | null>(null);
  const [watchlist, setWatchlist] = useState<WatchlistItem[] | null>(null);
  const [card, setCard] = useState<CreditCardOverview | null>(null);

  const [range, setRange] = useState<Range>("1Y");
  const [error, setError] = useState<string | null>(null);

  const loadStatic = useCallback(async () => {
    try {
      const [profileRes, overviewRes, watchlistRes, cardRes] =
        await Promise.all([
          authFetch<Profile>("/me"),
          authFetch<Overview>("/investments/overview"),
          authFetch<WatchlistItem[]>("/watchlist"),
          authFetch<CreditCardOverview>("/credit-card/overview"),
        ]);
      setProfile(profileRes);
      setOverview(overviewRes);
      setWatchlist(watchlistRes);
      setCard(cardRes);
    } catch (err: any) {
      setError(err?.message ?? "Failed to load your investment data.");
    }
  }, []);

  const loadGrowth = useCallback(async (r: Range) => {
    try {
      const data = await authFetch<GrowthData>(`/investments/growth?range=${r}`);
      setGrowth(data);
    } catch (err: any) {
      setError(err?.message ?? "Failed to load growth chart.");
    }
  }, []);

  useEffect(() => {
    loadStatic();
  }, [loadStatic]);

  useEffect(() => {
    loadGrowth(range);
  }, [range, loadGrowth]);

  const todayChangePositive = (overview?.todayChangePercent ?? 0) >= 0;

  const allocationsWithColor = useMemo(
    () =>
      (overview?.allocations ?? []).map((a, i) => ({
        ...a,
        color: ALLOCATION_COLORS[i % ALLOCATION_COLORS.length],
      })),
    [overview]
  );

  return (
    <div className="min-h-screen w-full bg-[#0f0a26] bg-[radial-gradient(ellipse_at_top_left,_#1b1440_0%,_#0f0a26_60%)] text-white font-sans px-4 py-5 sm:px-8 sm:py-6">
      {error && (
        <div className="mb-4 rounded-xl border border-red-400/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {error}
        </div>
      )}

      {/* Top bar */}
      <header className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <h1 className="text-xl sm:text-2xl font-semibold">
          Good Evening, {profile?.firstName ?? "…"}
        </h1>

        <div className="flex items-center gap-3 flex-1 justify-end min-w-[260px]">
          <div className="hidden md:flex items-center gap-2 bg-white/5 border border-white/10 rounded-full px-4 py-2 w-full max-w-xs">
            <Search size={16} className="text-white/40" />
            <input
              placeholder="Search here…"
              className="bg-transparent outline-none text-sm placeholder:text-white/40 w-full"
            />
          </div>
          <button
            aria-label="Notifications"
            className="h-9 w-9 grid place-items-center rounded-full bg-white/5 border border-white/10 hover:bg-white/10 transition"
          >
            <Bell size={16} />
          </button>
          <button
            aria-label="Messages"
            className="h-9 w-9 grid place-items-center rounded-full bg-white/5 border border-white/10 hover:bg-white/10 transition"
          >
            <MessageSquare size={16} />
          </button>
          <button className="flex items-center gap-2 pl-1 pr-2 py-1 rounded-full hover:bg-white/5 transition">
            {profile?.avatarUrl ? (
              <img
                src={profile.avatarUrl}
                alt={profile.firstName}
                className="h-8 w-8 rounded-full object-cover"
              />
            ) : (
              <div className="h-8 w-8 rounded-full bg-white/10" />
            )}
            <span className="hidden sm:flex flex-col text-left leading-tight">
              <span className="text-xs font-medium">
                {profile?.firstName ?? "…"}
              </span>
              <span className="text-[10px] text-white/40">
                {profile?.handle ?? ""}
              </span>
            </span>
            <ChevronDown size={14} className="text-white/40" />
          </button>
        </div>
      </header>

      {/* Row 1: Holdings + Growth */}
      <div className="grid grid-cols-1 lg:grid-cols-[380px_1fr] gap-5 mb-5">
        {/* Overview of Holdings */}
        <section className="rounded-2xl bg-white/[0.04] border border-white/10 p-5">
          <div className="flex items-start justify-between mb-4">
            <div>
              <h2 className="font-semibold">Overview of Your Holdings</h2>
              <p className="text-xs text-white/40">Monitor Your Investment Journey</p>
            </div>
            <LayoutGrid size={16} className="text-white/30 mt-1" />
          </div>

          {overview ? (
            <>
              <div className="flex items-end justify-between mb-1">
                <span className="text-3xl font-semibold">
                  {formatCurrency(overview.totalValue, 2)}
                </span>
                <svg width="72" height="28" viewBox="0 0 72 28" fill="none">
                  <path
                    d="M2 22 L14 16 L24 20 L36 8 L48 12 L60 4 L70 9"
                    stroke="#4FD1A5"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    fill="none"
                  />
                </svg>
              </div>
              <p className="text-xs mb-4">
                <span className="text-white/40">Today's Change: </span>
                <span
                  className={
                    todayChangePositive ? "text-emerald-400" : "text-red-400"
                  }
                >
                  {formatSignedPercent(overview.todayChangePercent)} (
                  {formatCurrency(overview.todayChangeAmount)})
                </span>
                <span className="text-white/40"> · Monthly Return: </span>
                <span className="text-emerald-400">
                  {formatSignedPercent(overview.monthlyReturnPercent)} (
                  {formatCurrency(overview.monthlyReturnAmount)})
                </span>
              </p>

              <ul className="space-y-3">
                {allocationsWithColor.map((a) => (
                  <li
                    key={a.label}
                    className="flex items-center justify-between border-t border-white/5 pt-3 first:border-0 first:pt-0"
                  >
                    <div className="flex items-center gap-2">
                      <span
                        className="h-2 w-2 rounded-full"
                        style={{ backgroundColor: a.color }}
                      />
                      <div>
                        <p className="text-sm font-medium">{a.label}</p>
                        <p className="text-[11px] text-white/40">{a.percent}%</p>
                      </div>
                    </div>
                    <span className="text-sm font-medium">
                      {formatCurrency(a.value, 0)}
                    </span>
                  </li>
                ))}
              </ul>
            </>
          ) : (
            <div className="space-y-3">
              <CardSkeleton className="h-9 w-40" />
              <CardSkeleton className="h-4 w-56" />
              {Array.from({ length: 4 }).map((_, i) => (
                <CardSkeleton key={i} className="h-10 w-full" />
              ))}
            </div>
          )}
        </section>

        {/* Total Investment Growth */}
        <section className="rounded-2xl bg-white/[0.04] border border-white/10 p-5">
          <div className="flex flex-wrap items-start justify-between gap-3 mb-2">
            <div>
              <h2 className="font-semibold">Total Investment Growth</h2>
              <p className="text-xs text-white/40">Track Your Investment Growth</p>
            </div>
            <div className="flex items-center gap-1 bg-white/5 border border-white/10 rounded-full p-1">
              {RANGES.map((r) => (
                <button
                  key={r}
                  onClick={() => setRange(r)}
                  className={`text-xs px-3 py-1.5 rounded-full transition ${
                    range === r
                      ? "bg-[#7C6CF6] text-white"
                      : "text-white/50 hover:text-white"
                  }`}
                >
                  {r}
                </button>
              ))}
              <LayoutGrid size={14} className="text-white/30 ml-2 mr-1" />
            </div>
          </div>

          {growth ? (
            <GrowthChart data={growth} />
          ) : (
            <CardSkeleton className="h-[280px] w-full" />
          )}
        </section>
      </div>

      {/* Row 2: Watchlist + Credit Card */}
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-5">
        {/* Watchlist */}
        <section className="rounded-2xl bg-white/[0.04] border border-white/10 p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold">My Watchlist</h2>
            <button className="flex items-center gap-1 text-xs text-white/50 hover:text-white transition">
              <LayoutGrid size={13} /> All Assets
            </button>
          </div>

          {watchlist ? (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-white/40 text-xs">
                  <th className="font-normal pb-3">Title</th>
                  <th className="font-normal pb-3">Price</th>
                  <th className="font-normal pb-3">Change</th>
                  <th className="font-normal pb-3">Market Cap</th>
                </tr>
              </thead>
              <tbody>
                {watchlist.map((item) => {
                  const positive = item.changePercent >= 0;
                  return (
                    <tr
                      key={item.symbol}
                      className="border-t border-white/5 hover:bg-white/[0.03] transition"
                    >
                      <td className="py-3">
                        <div className="flex items-center gap-2.5">
                          <div className="h-8 w-8 rounded-full bg-white/10 grid place-items-center overflow-hidden">
                            {item.iconUrl ? (
                              <img
                                src={item.iconUrl}
                                alt={item.symbol}
                                className="h-full w-full object-cover"
                              />
                            ) : (
                              <Bitcoin size={16} className="text-white/50" />
                            )}
                          </div>
                          <div>
                            <p className="font-medium">{item.symbol}</p>
                            <p className="text-[11px] text-white/40">{item.name}</p>
                          </div>
                        </div>
                      </td>
                      <td className="py-3 font-medium">
                        {formatCurrency(item.price, 2)}
                      </td>
                      <td className="py-3">
                        <span
                          className={`inline-flex items-center gap-1 font-medium ${
                            positive ? "text-emerald-400" : "text-red-400"
                          }`}
                        >
                          {positive ? (
                            <TrendingUp size={13} />
                          ) : (
                            <TrendingDown size={13} />
                          )}
                          {formatSignedPercent(item.changePercent)}
                        </span>
                      </td>
                      <td className="py-3 text-white/70">{item.marketCap}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          ) : (
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <CardSkeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          )}
        </section>

        {/* Credit Card Overview */}
        <section className="rounded-2xl bg-white/[0.04] border border-white/10 p-5">
          <h2 className="font-semibold mb-4">Credit Card Overview</h2>

          {card ? (
            <>
              <div className="rounded-2xl overflow-hidden bg-gradient-to-br from-[#0b1c2e] to-[#0b1c2e] border border-white/10">
                <div className="h-32 bg-gradient-to-br from-cyan-400 via-teal-400 to-emerald-400 p-4 flex flex-col justify-between relative">
                  <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
                    <path
                      d="M14 2C7.4 2 2 7.4 2 14s5.4 12 12 12 12-5.4 12-12S20.6 2 14 2zm0 4c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2zm0 16c-3.3 0-6.2-1.7-8-4.2.1-2.7 5.3-4.1 8-4.1s7.9 1.4 8 4.1c-1.8 2.5-4.7 4.2-8 4.2z"
                      fill="white"
                      opacity="0.9"
                    />
                  </svg>
                  <p className="tracking-widest text-sm font-medium text-black/80">
                    {card.maskedNumber}
                  </p>
                </div>
                <div className="bg-[#0b1c2e] px-4 py-3 flex justify-between text-[11px] text-white/50">
                  <div>
                    <p className="uppercase">Exp</p>
                    <p className="text-white text-xs">{card.expiry}</p>
                  </div>
                  <div>
                    <p className="uppercase">Cvv</p>
                    <p className="text-white text-xs">{card.cvvMasked}</p>
                  </div>
                </div>
              </div>

              <div className="flex justify-between mt-4">
                <div>
                  <p className="text-[11px] text-white/40">Balance Available</p>
                  <p className="font-semibold">
                    {formatCurrency(card.balanceAvailable, 0)}
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-[11px] text-white/40">Credit Limit</p>
                  <p className="font-semibold">
                    {formatCurrency(card.creditLimit, 0)}
                  </p>
                </div>
              </div>
            </>
          ) : (
            <CardSkeleton className="h-52 w-full" />
          )}
        </section>
      </div>
    </div>
  );
}