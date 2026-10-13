import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { FormControl, InputLabel, MenuItem, Select } from '@mui/material'
import { fetchOrganizerApplications } from './api'
import type { EditedFilter, StatusFilter } from './api'
import './App.css'

function formatDate(iso: string | null): string {
  return iso ? new Date(iso).toLocaleString() : '--'
}

export default function App() {
  const [status, setStatus] = useState<StatusFilter>('ALL')
  const [edited, setEdited] = useState<EditedFilter>('ALL')

  // При смене status или edited запрос выполняется заново
  const { data, isLoading } = useQuery({
    queryKey: ['organizerApplications', status, edited],
    queryFn: () => fetchOrganizerApplications(status, edited),
  })

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

      {isLoading && <p>Загрузка...</p>}
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
