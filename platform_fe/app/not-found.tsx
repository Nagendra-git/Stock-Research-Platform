import Link from "next/link";

// Next.js auto-renders this for any unmatched route — no catch-all
// <Route path="*" /> needed like in react-router-dom.
export default function NotFound() {
  return (
    <div>
      <h1>404 — Page not found</h1>
      <p>
        <Link href="/dashboard">Back to Dashboard</Link>
      </p>
    </div>
  );
}
