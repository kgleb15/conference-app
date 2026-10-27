import { useEffect, useState } from 'react'
import { Button, FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'
import { ApiError, fetchOrganizerApplications } from './api'
import { getPersonId, setPersonId } from './auth'
import type { EditedFilter, StatusFilter } from './api'
import type { Application, ApplicationStatus } from './types'
import ApplicationDialog from './ApplicationDialog'
import { STATUS_LABELS } from './statuses'
import './App.css'

function formatDate(iso: string | null): string {
  return iso ? new Date(iso).toLocaleString() : '--'
}

function errorText(e: unknown): string {
  if (e instanceof ApiError && e.status === 401) {
    return 'Id не указан (неизвестен)'
  }
  if (e instanceof ApiError && e.status === 403) {
    return 'У этого пользователя нет прав организатора'
  }
  return 'Не удалось загрузить заявки'
}

export default function App() {
  const [status, setStatus] = useState<StatusFilter>('ALL')
  const [edited, setEdited] = useState<EditedFilter>('ALL')
  const [data, setData] = useState<Application[] | null>(null)
  const [personId, setPersonIdState] = useState(getPersonId()) // применённый id
  const [draftId, setDraftId] = useState(getPersonId()) // то, что набрано в поле
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<Application | null>(null) // открытая заявка

  // При смене фильтров или id запрос выполняется заново
  useEffect(() => {
    fetchOrganizerApplications(status, edited).then((apps) => {
      setData(apps)
      setError(null)
    })
      .catch((e) => {
        setData(null)
        setError(errorText(e))
      })
  }, [status, edited, personId])

  return (
    <div className="page">
      <h1>Заявки на конференцию</h1>

      <div className="person">
        <TextField size="small" label="Id организатора" className="person-input"
          value={draftId} onChange={(e) => setDraftId(e.target.value)} />
        <Button variant="contained" onClick={() => {
          setPersonId(draftId)
          setPersonIdState(draftId.trim())
        }}>Применить</Button>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="filters">
        <FormControl size="small" className="filter">
          <InputLabel id="status-label">Статус</InputLabel>
          <Select labelId="status-label" label="Статус" value={status}
            onChange={(e) => setStatus(e.target.value as StatusFilter)}>
            <MenuItem value="ALL">Все заявки</MenuItem>
            {(Object.keys(STATUS_LABELS) as ApplicationStatus[]).map((s) => (
              <MenuItem key={s} value={s}>{STATUS_LABELS[s]}</MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl size="small" className="filter">
          <InputLabel id="edited-label">Изменения</InputLabel>
          <Select labelId="edited-label" label="Изменения" value={edited}
            onChange={(e) => setEdited(e.target.value as EditedFilter)}>
            <MenuItem value="ALL">Все заявки</MenuItem>
            <MenuItem value="true">Только изменённые</MenuItem>
            <MenuItem value="false">Не менялись</MenuItem>
          </Select>
        </FormControl>
      </div>

      {data === null && error === null && <p>Загрузка...</p>}
      {data && data.length === 0 && <p>Заявок нет</p>}

      {data && data.length > 0 && (
        <table>
          <thead>
            <tr> <th>Заявка</th> <th>Автор</th> <th>Контакты</th> <th>Статус</th> <th>Подана</th>
              <th>Правок</th> <th>Последняя правка</th> <th>Отозвана</th> <th>Последнее решение</th>
            </tr>
          </thead>
          <tbody>
            {data.map((a) => (
              <tr key={a.id} className="row" onClick={() => setSelected(a)}>
                <td>{a.title}</td>
                <td> {a.owner.fullName}<br />{a.owner.groupName}</td>
                <td>{a.owner.email}<br />{a.owner.phone}</td>
                <td className={`status status-${a.status}`}>{STATUS_LABELS[a.status]}</td>
                <td>{formatDate(a.createdAt)}</td>
                <td>{a.editCount}</td>
                <td>{formatDate(a.lastEditedAt)}</td>
                <td>{formatDate(a.withdrawnAt)}</td>
                <td>
                  {a.decidedByName ? (<>{a.decidedByName}<br />{formatDate(a.decidedAt)}</>) : '--'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <ApplicationDialog application={selected} onClose={() => setSelected(null)} />
    </div>
  )
}
