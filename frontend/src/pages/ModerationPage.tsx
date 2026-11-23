import { useEffect, useState } from 'react'
import { FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'
import { ApiError, fetchOrganizerApplications } from '../api/api'
import type { EditedFilter, StatusFilter } from '../api/api'
import type { Application, ApplicationStatus } from '../common/types'
import DirectionLabel from '../components/DirectionLabel'
import ApplicationDialog from '../components/ApplicationDialog'
import { formatDate } from '../common/utils'
import { STATUS_LABELS } from '../common/statuses'
import '../styles/App.css'

function errorText(e: unknown): string {
  if (e instanceof ApiError && e.status === 401) {
    return 'Нужно войти заново'
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
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<Application | null>(null) // открытая заявка
  const [reload, setReload] = useState(0) // увеличиваем, чтобы заново загрузить список
  const [searchText, setSearchText] = useState('') // то, что набрано в поле поиска
  const [search, setSearch] = useState('') // поиск, который применяется

  // Debounce 500 мс
  useEffect(() => {
    const timer = setTimeout(() => setSearch(searchText), 500)
    return () => clearTimeout(timer)
  }, [searchText])

  const query = search.trim().toLowerCase()
  const shown = data === null ? null : data.filter((a) =>
    query === '' || a.title.toLowerCase().includes(query)
    || a.authors.some((x) => x.person.fullName.toLowerCase().includes(query)))

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
  }, [status, edited, reload])

  return (
    <div className="page">
      <h1>Заявки на конференцию</h1>
      <DirectionLabel />

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

        <TextField size="small" label="Поиск по названию или автору" style={{ minWidth: 320 }}
          value={searchText} onChange={(e) => setSearchText(e.target.value)} />
      </div>

      {data === null && error === null && <p>Загрузка...</p>}
      {data && data.length === 0 && <p>Заявок нет</p>}
      {data && data.length > 0 && shown && shown.length === 0 && <p>Ничего не найдено</p>}

      {shown && shown.length > 0 && (
        <table>
          <thead>
            <tr> <th>Заявка</th> <th>Авторы</th> <th>Контакты</th> <th>Статус</th> <th>Подана</th>
              <th>Правок</th> <th>Последняя правка</th> <th>Отозвана</th> <th>Последнее решение</th>
            </tr>
          </thead>
          <tbody>
            {shown.map((a) => (
              <tr key={a.id} className="row" onClick={() => setSelected(a)}>
                <td>{a.title}</td>
                <td>
                  {a.authors.map((x) => (
                    <div key={x.person.id}>{x.person.fullName}<br />{x.groupName}</div>
                  ))}
                </td>
                <td>
                  {a.authors.map((x) => (
                    <div key={x.person.id}>{x.person.email}<br />{x.person.phone}</div>
                  ))}
                </td>
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

      <ApplicationDialog application={selected} onClose={() => { setSelected(null) }}
        onChanged={() => setReload((r) => r + 1)}
      />
    </div>
  )
}
