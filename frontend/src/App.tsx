import { useEffect, useState } from 'react'
import { FormControl, InputLabel, MenuItem, Select } from '@mui/material'
import { fetchOrganizerApplications } from './api'
import type { EditedFilter, StatusFilter } from './api'
import type { Application } from './types'
import './App.css'

function formatDate(iso: string | null): string {
  return iso ? new Date(iso).toLocaleString() : '--'
}

export default function App() {
  const [status, setStatus] = useState<StatusFilter>('ALL')
  const [edited, setEdited] = useState<EditedFilter>('ALL')
  const [data, setData] = useState<Application[] | null>(null)

  // При смене status или edited запрос выполняется заново
  useEffect(() => {
    fetchOrganizerApplications(status, edited).then((apps) => {
      setData(apps)
    })
  }, [status, edited])

  return (
    <div className="page">
      <h1>Заявки на конференцию</h1>

      <div className="filters">
        <FormControl size="small" className="filter">
          <InputLabel id="status-label">Статус</InputLabel>
          <Select labelId="status-label" label="Статус" value={status}
            onChange={(e) => setStatus(e.target.value as StatusFilter)}>
            <MenuItem value="ALL">Все заявки</MenuItem>
            <MenuItem value="SUBMITTED">Поданные</MenuItem>
            <MenuItem value="WITHDRAWN">Отозванные</MenuItem>
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

      {data === null && <p>Загрузка...</p>}
      {data && data.length === 0 && <p>Заявок нет</p>}

      {data && data.length > 0 && (
        <table>
          <thead>
            <tr> <th>Заявка</th> <th>Автор</th> <th>Контакты</th> <th>Статус</th> <th>Подана</th>
                 <th>Правок</th> <th>Последняя правка</th> <th>Отозвана</th>
            </tr>
          </thead>
          <tbody>
            {data.map((a) => (
              <tr key={a.id}>
                <td>{a.title}</td>
                <td> {a.owner.fullName}<br />{a.owner.groupName}</td>
                <td>{a.owner.email}<br />{a.owner.phone}</td>
                <td className={a.status === 'SUBMITTED' ? 'green' : 'gray'}>
                  {a.status === 'SUBMITTED' ? 'Подана' : 'Отозвана'}
                </td>
                <td>{formatDate(a.createdAt)}</td>
                <td>{a.editCount}</td>
                <td>{formatDate(a.lastEditedAt)}</td>
                <td>{formatDate(a.withdrawnAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
