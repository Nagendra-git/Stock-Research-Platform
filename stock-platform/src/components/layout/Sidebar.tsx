import { Link, useLocation } from "react-router-dom";
import "./Sidebar.css"
import {
  LayoutDashboard,
  BrainCog,
  CreditCard,
  PieChart,
  Target,
  BarChart3,
  FileBarChart,
  Newspaper,
  Globe,
  Bell,
  ChartNoAxesCombined,
  TrendingUp,
  type LucideIcon,
} from "lucide-react";

type NavItem = {
  label: string;
  icon: LucideIcon;
  href: string;
};

const mainNav: NavItem[] = [
  { label: "Dashboard", icon: LayoutDashboard, href: "/dashboard" },
  { label: "AI Advisor", icon: BrainCog, href: "/ai-advisor" },
];

const moneyManagementNav: NavItem[] = [
  { label: "Accounts", icon: CreditCard, href: "/accounts" },
  { label: "Budgeting", icon: PieChart, href: "/budgeting" },
  { label: "Goals & Planning", icon: Target, href: "/goals-planning" },
  { label: "Investments", icon: BarChart3, href: "/investments" },
];

const insightsNav: NavItem[] = [
  { label: "Weekly Trend", icon: TrendingUp, href: "/weekly-trend" },
  { label: "Quarterly Trend", icon: ChartNoAxesCombined, href: "/quarterly-trend" },
  { label: "Reports & Analytics", icon: FileBarChart, href: "/reports" },
  { label: "News Digest", icon: Newspaper, href: "/news" },
  { label: "Community Insights", icon: Globe, href: "/community" },
];

const supportNav: NavItem[] = [
  { label: "Notifications", icon: Bell, href: "/notifications" },
];

function Brand() {
  return (
    <div className="brand">
      <div className="brand-mark">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="white"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M12 20V10" />
          <path d="M12 10c0-3.5-2.5-6-7-6 0 4.5 2.5 7 7 7Z" />
          <path d="M12 13c0-3.5 2.5-6 7-6 0 4.5-2.5 7-7 7Z" />
        </svg>
      </div>

      <span className="brand-name">Sprout</span>
    </div>
  );
}

type NavGroupProps = {
  title: string;
  items: NavItem[];
  pathname: string;
};

function NavGroup({ title, items, pathname }: NavGroupProps) {
  return (
    <div className="nav-group">
      <p className="nav-group-title">{title}</p>

      <ul className="nav-list">
        {items.map((item) => {
          const isActive =
            pathname === item.href ||
            pathname.startsWith(item.href + "/");

          return (
            <li key={item.label} className="nav-item">
              <Link
                to={item.href}
                className={`nav-link ${isActive ? "active" : ""}`}
              >
                <item.icon className="nav-icon" size={18} />
                <span className="nav-text">{item.label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

export default function Sidebar() {
  const location = useLocation();

  return (
    <aside className="sidebar">
      <Brand />

      <NavGroup
        title="Main"
        items={mainNav}
        pathname={location.pathname}
      />

      <NavGroup
        title="Money Management"
        items={moneyManagementNav}
        pathname={location.pathname}
      />

      <NavGroup
        title="Insights"
        items={insightsNav}
        pathname={location.pathname}
      />

      <NavGroup
        title="Support & Settings"
        items={supportNav}
        pathname={location.pathname}
      />
    </aside>
  );
}