import { Suspense, lazy } from "react";
import { Routes, Route, Navigate } from "react-router-dom";

// Lazy-loaded pages: this is the Vite/CRA equivalent of Next.js's automatic
// code-splitting. Each import() becomes its own chunk, loaded on demand.
const WeeklyTrendPage = lazy(
  () => import("../pages/WeeklyTrend/WeeklyTrend")
);


// Placeholder pages until they get their own folders — swap these out the
// same way WeeklyTrendPage was swapped in above.
const Dashboard = () => <h1>Dashboard</h1>;
const AiAdvisor = () => <h1>AI Advisor</h1>;
const Accounts = () => <h1>Accounts</h1>;
const Budgeting = () => <h1>Budgeting</h1>;
const CommunityInsights = () => <h1>Community Insights</h1>;
const GoalsPlanning = () => <h1>Goals & Planning</h1>;
const Investments = () => <h1>My Investments</h1>;
const QuarterlyTrend = () => <h1>Quarterly Trend</h1>;
const ReportsAnalytics = () => <h1>Reports & Analytics</h1>;
const NewsDigest = () => <h1>News Digest</h1>;
const Notifications = () => <h1>Notifications</h1>;

export default function AppRoutes() {
  return (
    <Suspense fallback={<p>Loading...</p>}>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/ai-advisor" element={<AiAdvisor />} />
        <Route path="/accounts" element={<Accounts />} />
        <Route path="/budgeting" element={<Budgeting />} />
        <Route path="/community-insights" element={<CommunityInsights />} />
        <Route path="/goals-planning" element={<GoalsPlanning />} />
        <Route path="/investments" element={<Investments />} />
        <Route path="/weekly-trend" element={<WeeklyTrendPage />} />
        <Route path="/quarterly-trend" element={<QuarterlyTrend />} />
        <Route path="/reports-analytics" element={<ReportsAnalytics />} />
        <Route path="/news-digest" element={<NewsDigest />} />
        <Route path="/notifications" element={<Notifications />} />
      </Routes>
    </Suspense>
  );
}
