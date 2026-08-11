import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import DashboardPage from './pages/dashboard/DashboardPage'
import InventoryPage from './pages/inventory/InventoryPage'
import PlaceholderPage from './pages/PlaceholderPage'
import ProductFormPage from './pages/products/ProductFormPage'
import ProductListPage from './pages/products/ProductListPage'
import NewSalePage from './pages/sales/NewSalePage'
import SaleDetailPage from './pages/sales/SaleDetailPage'
import SalesPage from './pages/sales/SalesPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<DashboardPage />} />
          <Route path="products" element={<ProductListPage />} />
          <Route path="products/new" element={<ProductFormPage />} />
          <Route path="products/:id/edit" element={<ProductFormPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="sales" element={<SalesPage />} />
          <Route path="sales/new" element={<NewSalePage />} />
          <Route path="sales/:id" element={<SaleDetailPage />} />
          <Route path="reports" element={<PlaceholderPage title="Reports" />} />
          <Route path="settings" element={<PlaceholderPage title="Settings" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App