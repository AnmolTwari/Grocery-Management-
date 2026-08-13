import { api } from './api'

export const getReportSummary = (from, to) => {
  const params = new URLSearchParams()
  if (from) params.set('from', from)
  if (to) params.set('to', to)
  const query = params.toString()
  return api.get(`/reports/summary${query ? `?${query}` : ''}`)
}