"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
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
  HelpCircle,
  SlidersHorizontal,
  type LucideIcon,
} from "lucide-react";
import "../styles/sidebar.css";

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
  { label: "Reports & Analytics", icon: FileBarChart, href: "/reports-analytics" },
  { label: "News Digest", icon: Newspaper, href: "/news-digest" },
  { label: "Community Insights", icon: Globe, href: "/community-insights" },
];

const supportNav: NavItem[] = [
  { label: "Notifications", icon: Bell, href: "/notifications" },
  { label: "Help & Support", icon: HelpCircle, href: "/help-support" },
  { label: "Settings", icon: SlidersHorizontal, href: "/settings" },
];

function Brand() {
  return (
    <div className="brand">
      <div className="brand-mark">
        <svg viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 20V10" />
          <path d="M12 10c0-3.5-2.5-6-7-6 0 4.5 2.5 7 7 7Z" />
          <path d="M12 13c0-3.5 2.5-6 7-6 0 4.5-2.5 7-7 7Z" />
        </svg>
      </div>
      <span className="brand-name">Sprout</span>
    </div>
  );
}

function NavGroup({ title, items, pathname }: { title: string; items: NavItem[]; pathname: string }) {
  return (
    <div className="nav-group">
      <p className="nav-group-title">{title}</p>

      <ul className="nav-list">
        {items.map((item) => {
          const isActive = pathname === item.href || pathname.startsWith(item.href + "/");

          return (
            <li key={item.label} className="nav-item">
              <Link href={item.href} className={`nav-link ${isActive ? "active" : ""}`}>
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
  const pathname = usePathname();

  return (
    <aside className="sidebar">
      <Brand />
      <NavGroup title="Main" items={mainNav} pathname={pathname} />
      <NavGroup title="Money Management" items={moneyManagementNav} pathname={pathname} />
      <NavGroup title="Insights" items={insightsNav} pathname={pathname} />
      <NavGroup title="Support & Settings" items={supportNav} pathname={pathname} />
    </aside>
  );
}
