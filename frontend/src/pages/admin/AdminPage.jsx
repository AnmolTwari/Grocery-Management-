import { useEffect, useState } from 'react'
import RefreshButton from '../../components/RefreshButton'
import {
  createUser,
  deleteUser,
  getAnalytics,
  listUsers,
  updateUser,
} from '../../services/admin'
import { api, auth } from '../../services/api'
import { formatCurrency, formatDateTime } from '../../utils/format'

const PAGE_SIZE = 20

function passwordHashLabel(hash) {
  if (!hash) return '—'
  return `${hash.slice(0, 22)}…`
}

function AnalyticsCard({ label, value, hint }) {
  return (
    <div className="rounded-lg border border-border bg-surface p-4 shadow-sm">
      <div className="text-xs font-semibold tracking-wider text-muted uppercase">{label}</div>
      <div className="mt-1 text-[22px] font-bold">{value}</div>
      {hint && <div className="mt-0.5 text-xs text-secondary">{hint}</div>}
    </div>
  )
}

export default function AdminPage() {
  const [analytics, setAnalytics] = useState(null)
  const [users, setUsers] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [reload, setReload] = useState(0)
  const [createForm, setCreateForm] = useState({
    username: '',
    email: '',
    password: '',
    role: 'USER',
  })
  const [creating, setCreating] = useState(false)

  const currentUserId = auth.getCurrentUser()?.username

  function resetCreateForm() {
    setCreateForm({ username: '', email: '', password: '', role: 'USER' })
  }

  async function handleCreateUser(event) {
    event.preventDefault()
    setError(null)
    setSuccess(null)
    setCreating(true)
    try {
      const created = await createUser(createForm)
      setSuccess(`Created user "${created.username}".`)
      resetCreateForm()
      setReload((value) => value + 1)
    } catch (err) {
      setError(err.message)
    } finally {
      setCreating(false)
    }
  }

  useEffect(() => {
    let cancelled = false
    getAnalytics()
      .then((data) => {
        if (!cancelled) setAnalytics(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
    return () => {
      cancelled = true
    }
  }, [reload])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    listUsers({ page, size: PAGE_SIZE })
      .then((data) => {
        if (cancelled) return
        if (data.empty && page > 0) {
          setPage(page - 1)
          return
        }
        setUsers(data)
        setError(null)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [page, reload])

  async function handleUpdate(user, changes) {
    setError(null)
    setSuccess(null)
    try {
      await updateUser(user.id, changes)
      setSuccess(`Updated ${user.username}.`)
      setReload((value) => value + 1)
    } catch (err) {
      setError(err.message)
    }
  }

  function handleRefresh() {
    api.clearCache()
    setReload((value) => value + 1)
  }

  async function handleDelete(user) {
    if (!window.confirm(`Delete user "${user.username}" permanently? This cannot be undone.`)) {
      return
    }
    setError(null)
    setSuccess(null)
    try {
      await deleteUser(user.id)
      setSuccess(`Deleted ${user.username}.`)
      setReload((value) => value + 1)
    } catch (err) {
      setError(err.message)
    }
  }

  const isLoading = !users && loading
  const isEmpty = users && users.content.length === 0

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">Admin</h1>
        <RefreshButton onClick={handleRefresh} disabled={loading} />
      </div>

      {error && (
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      )}
      {success && !error && (
        <div className="rounded-sm border border-[#bbf7d0] bg-primary-light px-4 py-3 text-sm text-[#166534]">
          {success}
        </div>
      )}

      <div>
        <h2 className="mb-3 text-base font-semibold">Analytics</h2>
        {!analytics ? (
          <p className="text-secondary">Loading…</p>
        ) : (
          <div className="grid grid-cols-1 gap-3 min-[481px]:grid-cols-2 md:grid-cols-3 xl:grid-cols-5">
            <AnalyticsCard label="Users" value={analytics.totalUsers} hint={`${analytics.activeUsers} active · ${analytics.adminUsers} admin`} />
            <AnalyticsCard label="Products" value={analytics.totalProducts} hint={`${analytics.lowStockProducts} low stock · ${analytics.outOfStockProducts} out of stock`} />
            <AnalyticsCard label="Sales" value={analytics.totalSales} hint={`${analytics.salesToday} today`} />
            <AnalyticsCard label="Total Revenue" value={formatCurrency(analytics.totalRevenue)} hint="All time" />
            <AnalyticsCard label="Revenue Today" value={formatCurrency(analytics.revenueToday)} />
          </div>
        )}
      </div>

      <div>
        <h2 className="mb-3 text-base font-semibold">Create user</h2>
        <form
          className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6"
          onSubmit={handleCreateUser}
        >
          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div className="field">
              <label htmlFor="new-username" className="mb-1 block text-sm font-medium">
                Username
              </label>
              <input
                id="new-username"
                className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
                required
                minLength={3}
                maxLength={50}
                autoComplete="off"
                placeholder="e.g. staff"
                value={createForm.username}
                onChange={(event) =>
                  setCreateForm((form) => ({ ...form, username: event.target.value }))
                }
              />
            </div>
            <div className="field">
              <label htmlFor="new-email" className="mb-1 block text-sm font-medium">
                Email
              </label>
              <input
                id="new-email"
                type="email"
                className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
                required
                maxLength={255}
                autoComplete="off"
                placeholder="staff@example.com"
                value={createForm.email}
                onChange={(event) =>
                  setCreateForm((form) => ({ ...form, email: event.target.value }))
                }
              />
            </div>
            <div className="field">
              <label htmlFor="new-password" className="mb-1 block text-sm font-medium">
                Password
              </label>
              <input
                id="new-password"
                type="password"
                className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
                required
                minLength={6}
                maxLength={100}
                autoComplete="new-password"
                placeholder="At least 6 characters"
                value={createForm.password}
                onChange={(event) =>
                  setCreateForm((form) => ({ ...form, password: event.target.value }))
                }
              />
            </div>
            <div className="field">
              <label htmlFor="new-role" className="mb-1 block text-sm font-medium">
                Role
              </label>
              <select
                id="new-role"
                className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
                value={createForm.role}
                onChange={(event) =>
                  setCreateForm((form) => ({ ...form, role: event.target.value }))
                }
              >
                <option value="USER">User (shop account)</option>
                <option value="ADMIN">Admin (full access)</option>
              </select>
            </div>
          </div>
          <div className="mt-4 flex justify-end">
            <button
              type="submit"
              className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
              disabled={creating}
            >
              {creating ? 'Creating…' : 'Create user'}
            </button>
          </div>
        </form>
      </div>

      <div>
        <h2 className="mb-3 text-base font-semibold">Users</h2>
        {isEmpty && !loading && (
          <div className="rounded-lg border border-border bg-surface p-8 text-center text-secondary shadow-sm">
            <p>No users yet.</p>
          </div>
        )}

        {users && !isEmpty && (
          <div className="overflow-x-auto rounded-lg border border-border bg-surface shadow-sm">
            <table className="w-full min-w-[800px] border-collapse [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
              <thead>
                <tr>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Username
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Email
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Role
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Status
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Created
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Password hash
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {users.content.map((user) => {
                  const isSelf = user.username === currentUserId
                  const isAdmin = user.role === 'ADMIN'
                  return (
                    <tr key={user.id}>
                      <td className="border-b border-border p-3 text-left align-middle">
                        <div className="font-semibold">
                          {user.username}
                          {isSelf && (
                            <span className="ml-2 rounded-full bg-primary-light px-2 py-0.5 text-xs font-semibold text-[#166534]">
                              You
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="border-b border-border p-3 text-left align-middle">
                        {user.email || <span className="text-muted">—</span>}
                      </td>
                      <td className="border-b border-border p-3 text-left align-middle">
                        <span
                          className={`inline-block rounded-full px-2 py-0.5 text-xs font-semibold whitespace-nowrap ${isAdmin ? 'bg-[#ede9fe] text-[#5b21b6]' : 'bg-[#e0f2fe] text-[#075985]'}`}
                        >
                          {isAdmin ? 'ADMIN' : 'USER'}
                        </span>
                      </td>
                      <td className="border-b border-border p-3 text-left align-middle">
                        {user.enabled ? (
                          <span className="inline-block rounded-full bg-primary-light px-2 py-0.5 text-xs font-semibold text-[#166534]">
                            Active
                          </span>
                        ) : (
                          <span className="inline-block rounded-full bg-[#fecaca] px-2 py-0.5 text-xs font-semibold text-[#991b1b]">
                            Disabled
                          </span>
                        )}
                      </td>
                      <td className="border-b border-border p-3 text-left align-middle">
                        {formatDateTime(user.createdAt)}
                      </td>
                      <td className="border-b border-border p-3 text-left align-middle">
                        <span
                          className="cursor-help text-xs text-secondary"
                          title={user.passwordHash || 'No hash stored'}
                        >
                          {passwordHashLabel(user.passwordHash)}
                        </span>
                      </td>
                      <td className="flex items-center gap-2 border-b border-border p-3 text-left align-middle whitespace-nowrap">
                        {isAdmin ? (
                          <button
                            type="button"
                            className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-danger transition-colors hover:enabled:border-danger hover:enabled:bg-[#fef2f2] disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                            disabled={isSelf}
                            title={isSelf ? 'You cannot demote your own account.' : 'Make this user a regular user'}
                            onClick={() => handleUpdate(user, { role: 'USER' })}
                          >
                            Demote
                          </button>
                        ) : (
                          <button
                            type="button"
                            className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-3 py-1 text-[13px] font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                            onClick={() => handleUpdate(user, { role: 'ADMIN' })}
                          >
                            Promote
                          </button>
                        )}
                        {user.enabled ? (
                          <button
                            type="button"
                            className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                            disabled={isSelf}
                            title={isSelf ? 'You cannot disable your own account.' : 'Disable this account'}
                            onClick={() => handleUpdate(user, { enabled: false })}
                          >
                            Disable
                          </button>
                        ) : (
                          <button
                            type="button"
                            className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                            onClick={() => handleUpdate(user, { enabled: true })}
                          >
                            Enable
                          </button>
                        )}
                        <button
                          type="button"
                          className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-danger transition-colors hover:enabled:border-danger hover:enabled:bg-[#fef2f2] disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                          disabled={isSelf}
                          title={isSelf ? 'You cannot delete your own account.' : 'Delete this user permanently'}
                          onClick={() => handleDelete(user)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}

        {users && users.totalPages > 1 && (
          <div className="mt-4 flex items-center justify-center gap-3">
            <button
              type="button"
              className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
              disabled={users.first}
              onClick={() => setPage(page - 1)}
            >
              Prev
            </button>
            <span className="text-sm text-secondary">
              Page {users.number + 1} of {users.totalPages}
            </span>
            <button
              type="button"
              className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
              disabled={users.last}
              onClick={() => setPage(page + 1)}
            >
              Next
            </button>
          </div>
        )}

        {isLoading && <p className="text-secondary">Loading…</p>}
      </div>
    </div>
  )
}
