import { useEffect, useState } from 'react'
import { Button } from '@mui/material'
import { ApiError, fetchMyApplications, withdrawApplication } from '../api/api'
import EditDialog from '../components/EditDialog'
import { STATUS_LABELS } from '../common/statuses'
import { formatDate } from '../common/utils'
import type { Application } from '../common/types'
import '../styles/App.css'

function withdrawErrorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 403) {
        return 'Срок отзыва заявки прошёл'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'По заявке уже принято решение, отозвать её нельзя'
    }
    return 'Не удалось отозвать заявку'
}

export default function MyApplicationsPage() {
    const [data, setData] = useState<Application[] | null>(null)
    const [error, setError] = useState<string | null>(null)
    const [editing, setEditing] = useState<Application | null>(null) // заявка в окне редактирования
    const [reload, setReload] = useState(0) // увеличиваем, чтобы заново загрузить список

    useEffect(() => {
        fetchMyApplications().then((apps) => {
            setData(apps)
            setError(null)
        }).catch(() => setError('Не удалось загрузить заявки'))
    }, [reload])

    async function withdraw(a: Application) {
        try {
            await withdrawApplication(a.id)
        } catch (e) {
            setError(withdrawErrorText(e))
        }
        setReload((r) => r + 1)
    }

    return (
        <div className="page">
            <h1>Мои заявки</h1>
            <div className="actions">
                <Button variant="contained" href="/submit">Подать заявку</Button>
                <span className="hint">На конкурс можно подать не более 2 заявок на одно направление</span>
            </div>

            {error && <p className="error">{error}</p>}
            {data === null && error === null && <p>Загрузка...</p>}
            {data && data.length === 0 && <p>У вас пока нет заявок</p>}

            {data && data.length > 0 && (
                <table>
                    <thead>
                        <tr> <th>Заявка</th> <th>Статус</th> <th>Подана</th> <th>Правок</th>
                            <th>Комментарий организатора</th> <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((a) => {
                            // изменять и отзывать можно, пока решение не принято
                            const canChange = a.status === 'SUBMITTED' || a.status === 'REVISION_REQUESTED'
                            return (
                                <tr key={a.id}>
                                    <td>{a.title}</td>
                                    <td className={`status status-${a.status}`}>{STATUS_LABELS[a.status]}</td>
                                    <td>{formatDate(a.createdAt)}</td>
                                    <td>{a.editCount}</td>
                                    <td>{a.moderationComment ?? '--'}</td>
                                    <td>
                                        {canChange && (<>
                                            <Button size="small" onClick={() => setEditing(a)}>Изменить</Button>
                                            <Button size="small" color="error" onClick={() => withdraw(a)}>Отозвать</Button>
                                        </>)}
                                    </td>
                                </tr>
                            )
                        })}
                    </tbody>
                </table>
            )}

            <EditDialog application={editing} onClose={() => setEditing(null)}
                onChanged={() => setReload((r) => r + 1)} />
        </div>
    )
}
