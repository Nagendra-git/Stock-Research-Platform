import Sidebar from "./components/Sidebar";
import "./styles/global.css";

export const metadata = {
  title: "Sprout",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="m-0 p-0">
        <div className="app-layout">
          <Sidebar />
          <main className="main-content">{children}</main>
        </div>
      </body>
    </html>
  );
}
