import { api } from './api'

/** Fetches the aggregated dashboard overview (today's sales, stock alerts, recent sales). */
export const getDashboardSummary = () => api.get('/dashboard/summary')