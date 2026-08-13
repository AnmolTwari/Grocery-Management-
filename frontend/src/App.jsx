import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import AuthPage from './pages/AuthPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import InventoryPage from './pages/inventory/InventoryPage'
import ProductFormPage from './pages/products/ProductFormPage'
import ProductListPage from './pages/products/ProductListPage'
import ReportsPage from './pages/reports/ReportsPage'
import NewSalePage from './pages/sales/NewSalePage'
import SaleDetailPage from './pages/sales/SaleDetailPage'
import SalesPage from './pages/sales/SalesPage'
import SettingsPage from './pages/settings/SettingsPage'
import { auth } from './services/api'

function ProtectedRoute({ children }) {
  if (!auth.isAuthenticated()) {
    return <Navigate to="/login" replace />
  }
  return children
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<AuthPage mode="login" />} />
        <Route path="/register" element={<AuthPage mode="register" />} />
        <Route element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }>
          <Route index element={<DashboardPage />} />
          <Route path="products" element={<ProductListPage />} />
          <Route path="products/new" element={<ProductFormPage />} />
          <Route path="products/:id/edit" element={<ProductFormPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="sales" element={<SalesPage />} />
          <Route path="sales/new" element={<NewSalePage />} />
          <Route path="sales/:id" element={<SaleDetailPage />} />
          <Route path="reports" element={<ReportsPage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App