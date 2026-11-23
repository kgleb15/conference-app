import { useEffect, useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from '@mui/material'
import { ApiError, changeTransfer, fetchDirections, moderateApplication, transferApplication } from '../api/api'
import type { DecisionAction, TransferAction } from '../api/api'
import { CATEGORY_LABELS, FUNDING_LABELS, STATUS_LABELS } from '../common/statuses'
import type { Application, Direction } from '../common/types'

type Props = {
    application: Application | null
    onClose: () => void
    onChanged: () => void // вызывается, когда нужно перезагрузить список
    incoming?: boolean
}

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 409) {
        return 'Решение по заявке уже нельзя принять'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Необходимы права администратора'
    }
    return 'Не удалось выполнить действие'
}

function transferErrorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Такого направления нет'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Заявку нельзя передать или у автора уже 2 заявки в этом направлении'
    }
    if (e instanceof ApiError && e.status === 404) {
        return 'Заявка не найдена'
    }
    return 'Не удалось передать заявку'
}

function changeTransferErrorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 409) {
        return 'Передачу уже нельзя изменить или у автора уже 2 заявки в этом направлении'
    }
    if (e instanceof ApiError && e.status === 404) {
        return 'Заявка не найдена или передача уже изменена'
    }
    return 'Не удалось выполнить действие'
}

export default function ApplicationDialog(props: Props) {
    const { application, onClose, onChanged, incoming = false } = props

    const [error, setError] = useState<string | null>(null)
    const [sending, setSending] = useState(false)
    const [comment, setComment] = useState('')
    const [directions, setDirections] = useState<Direction[]>([])
    const [targetDirection, setTargetDirection] = useState<number | ''>('')

    useEffect(() => {
        fetchDirections().then(setDirections).catch(() => setError('Не удалось загрузить направления'))
    }, [])

    function close() {
        setError(null)
        setComment('')
        setTargetDirection('')
        onClose()
    }

    async function decide(action: DecisionAction) {
        if (application === null) { return }
        setSending(true)
        setError(null)
        try {
            await moderateApplication(application.id, action, comment.trim())
            onChanged()
            close()
        } catch (e) {
            setError(errorText(e))
            onChanged()
        }
        setSending(false)
    }

    async function transfer() {
        if (application === null || targetDirection === '') { return }
        setSending(true)
        setError(null)
        try {
            await transferApplication(application.id, targetDirection)
            onChanged()
            close()
        } catch (e) {
            setError(transferErrorText(e))
            onChanged()
        }
        setSending(false)
    }

    async function doTransferAction(action: TransferAction) {
        if (application === null) { return }
        setSending(true)
        setError(null)
        try {
            await changeTransfer(application.id, action)
            onChanged()
            close()
        } catch (e) {
            setError(changeTransferErrorText(e))
            onChanged()
        }
        setSending(false)
    }

    return (
        <Dialog open={application !== null} onClose={onClose} maxWidth="md" fullWidth>
            {application && (<>
                <DialogTitle>{application.title}</DialogTitle>
                <DialogContent>
                    {application.authors.map((x, i) => (
                        <p key={x.person.id}>
                            <b>Автор {i + 1}:</b> {x.person.fullName}, {x.groupName}, {FUNDING_LABELS[x.fundingType]}
                        </p>
                    ))}
                    {application.supervisors.map((s, i) => (
                        <p key={i}><b>Научный руководитель {i + 1}:</b> {s.fullName}, {s.workplace}</p>
                    ))}
                    <p><b>Категория:</b> {CATEGORY_LABELS[application.applicantCategory]}</p>
                    <p><b>Статус:</b> {STATUS_LABELS[application.status]}</p>
                    {incoming && (
                        <p><b>Из направления:</b> {directions.find((d) => d.id === application.directionId)?.name ?? application.directionId}</p>
                    )}
                    {!incoming && application.status === 'TRANSFER_PENDING' && (
                        <p><b>Передаётся в направление:</b> {directions.find((d) => d.id === application.transferDirectionId)?.name ?? application.transferDirectionId}</p>
                    )}
                    {application.moderationComment && (
                        <p><b>Комментарий организатора:</b> {application.moderationComment}</p>
                    )}
                    <p><b>Аннотация:</b> {application.annotation}</p>
                    <p><b>Текст тезисов:</b></p> <div>{application.thesisText}</div>
                    {application.status === 'SUBMITTED' && (
                        <TextField label="Комментарий" fullWidth margin="normal" value={comment} onChange={(e) => setComment(e.target.value)} />
                    )}
                    {application.status === 'SUBMITTED' && (
                        <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 16 }}>
                            <TextField select label="Передать в направление" size="small" style={{ minWidth: 360 }}
                                value={directions.length > 0 ? targetDirection : ''}
                                onChange={(e) => setTargetDirection(Number(e.target.value))}>
                                {directions.filter((d) => d.id !== application.directionId).map((d) => (
                                    <MenuItem key={d.id} value={d.id}>{d.id}. {d.name}</MenuItem>
                                ))}
                            </TextField>
                            <Button disabled={sending || targetDirection === ''} onClick={transfer}>Передать</Button>
                        </div>
                    )}
                    {error && <p className="error">{error}</p>}
                </DialogContent>
                <DialogActions>
                    {application.status === 'SUBMITTED' && (<>
                        <Button color="success" disabled={sending} onClick={() => decide('approve')}>
                            Утвердить
                        </Button>
                        <Button color="warning" disabled={sending || comment.trim() === ''}
                            onClick={() => decide('request-revision')}>
                            На доработку
                        </Button>
                        <Button color="error" disabled={sending || comment.trim() === ''}
                            onClick={() => decide('reject')}>
                            Отклонить
                        </Button>
                    </>
                    )}
                    {incoming && (<>
                        <Button color="success" disabled={sending} onClick={() => doTransferAction('accept-transfer')}>
                            Принять
                        </Button>
                        <Button color="error" disabled={sending} onClick={() => doTransferAction('decline-transfer')}>
                            Отклонить передачу
                        </Button>
                    </>)}
                    {!incoming && application.status === 'TRANSFER_PENDING' && (
                        <Button color="warning" disabled={sending} onClick={() => doTransferAction('cancel-transfer')}>
                            Отменить передачу
                        </Button>
                    )}
                    <Button onClick={onClose}>Закрыть</Button>
                </DialogActions>
            </>)}
        </Dialog>
    )
}
