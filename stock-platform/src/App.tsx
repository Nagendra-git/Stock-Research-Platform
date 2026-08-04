import Sidebar from "./components/layout/Sidebar";
import AppRoutes from "./routes/AppRoutes";

function App() {
  return (
    <div className="app-layout">
      <Sidebar />

      <main className="main-content">
        <AppRoutes />
      </main>
    </div>
  );
}

export default App;