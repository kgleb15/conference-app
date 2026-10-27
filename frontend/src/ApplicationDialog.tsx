import { useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from '@mui/material'
import { ApiError, moderateApplication } from './api'
import type { DecisionAction } from './api'
import { STATUS_LABELS } from './statuses'
import type { Application } from './types'

type Props = {
    application: Application | null
    onClose: () => void
    onChanged: () => void // вызывается, когда нужно перезагрузить список
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

export default function ApplicationDialog(props: Props) {
    const { application, onClose, onChanged } = props

    const [error, setError] = useState<string | null>(null)
    const [sending, setSending] = useState(false)
    const [comment, setComment] = useState('')

    function close() {
        setError(null)
        setComment('')
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

    return (
        <Dialog open={application !== null} onClose={onClose} maxWidth="md" fullWidth>
            {application && (<>
                <DialogTitle>{application.title}</DialogTitle>
                <DialogContent>
                    <p><b>Автор:</b> {application.owner.fullName}, {application.owner.groupName}</p>
                    <p><b>Статус:</b> {STATUS_LABELS[application.status]}</p>
                    {application.moderationComment && (
                        <p><b>Комментарий организатора:</b> {application.moderationComment}</p>
                    )}
                    <p><b>Текст тезисов:</b></p> <div>{application.thesisText}</div>
                    {application.status === 'SUBMITTED' && (
                        <TextField label="Комментарий" fullWidth margin="normal" value={comment} onChange={(e) => setComment(e.target.value)} />
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
                    <Button onClick={onClose}>Закрыть</Button>
                </DialogActions>
            </>)}
        </Dialog>
    )
}
