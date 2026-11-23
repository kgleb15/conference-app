import { useEffect, useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { ApiError, fetchDirections, fetchMyApplications, withdrawApplication } from '../api/api'
import { getPerson } from '../api/auth'
import EditDialog from '../components/EditDialog'
import { STATUS_LABELS } from '../common/statuses'
import { formatDate } from '../common/utils'
import { Link } from 'react-router'
import type { Application, Direction } from '../common/types'
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
    const [withdrawing, setWithdrawing] = useState<Application | null>(null) // заявка, отзыв которой ждёт подтверждения
    const [reload, setReload] = useState(0) // увеличиваем, чтобы заново загрузить список
    const [directions, setDirections] = useState<Direction[]>([])
    const me = getPerson()

    useEffect(() => {
        fetchDirections().then(setDirections).catch(() => setError('Не удалось загрузить направления'))
    }, [])

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

    async function confirmWithdraw() {
        if (withdrawing === null) {
            return
        }
        const a = withdrawing
        setWithdrawing(null)
        await withdraw(a)
    }

    return (
        <div className="page">
            <h1>Мои заявки</h1>
            <div className="actions">
                <Button variant="contained" component={Link} to="/submit">Подать заявку</Button>
                <span className="hint">На конкурс можно подать не более 2 заявок на одно направление</span>
            </div>

            {error && <p className="error">{error}</p>}
            {data === null && error === null && <p>Загрузка...</p>}
            {data && data.length === 0 && <p>У вас пока нет заявок</p>}

            {data && data.length > 0 && (
                <table>
                    <thead>
                        <tr> <th>Заявка</th> <th>Направление</th> <th>Авторы</th> <th>Статус</th> <th>Подана</th> <th>Правок</th>
                            <th>Комментарий организатора</th> <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((a) => {
                            const isOwner = a.authors[0].person.id === me?.id
                            const canWithdraw = a.status === 'SUBMITTED' || a.status === 'REVISION_REQUESTED'
                            const canEdit = isOwner && canWithdraw
                            const direction = directions.find((d) => d.id === a.directionId)
                            return (
                                <tr key={a.id}>
                                    <td>{a.title}</td>
                                    <td>{direction ? `${direction.id}. ${direction.name}` : '--'}</td>
                                    <td>{a.authors.map((x) => x.person.fullName).join(', ')}</td>
                                    <td className={`status status-${a.status}`}>{STATUS_LABELS[a.status]}</td>
                                    <td>{formatDate(a.createdAt)}</td>
                                    <td>{a.editCount}</td>
                                    <td>{a.moderationComment ?? '--'}</td>
                                    <td>
                                        {canEdit && <Button size="small" onClick={() => setEditing(a)}>Изменить</Button>}
                                        {canWithdraw && (
                                            <Button size="small" color="error" onClick={() => setWithdrawing(a)}>Отозвать</Button>
                                        )}
                                    </td>
                                </tr>
                            )
                        })}
                    </tbody>
                </table>
            )}

            <Dialog open={withdrawing !== null} onClose={() => setWithdrawing(null)}>
                <DialogTitle>Отзыв заявки</DialogTitle>
                <DialogContent>Вы точно хотите отозвать заявку?</DialogContent>
                <DialogActions>
                    <Button onClick={() => setWithdrawing(null)}>Отмена</Button>
                    <Button color="error" onClick={confirmWithdraw}>Отозвать</Button>
                </DialogActions>
            </Dialog>

            <EditDialog application={editing} onClose={() => setEditing(null)}
                onChanged={() => setReload((r) => r + 1)} />
        </div>
    )
}
