import { useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { ApiError, moderateApplication } from './api'
import type { DecisionAction } from './api'
import { STATUS_LABELS } from './statuses'
import type { Application } from './types'

type Props = {
    application: Application | null
    onClose: () => void
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
    const { application, onClose } = props

    const [error, setError] = useState<string | null>(null)
    const [sending, setSending] = useState(false)

    function close() {
        setError(null)
        onClose()
    }

    async function decide(action: DecisionAction) {
        if (application === null) { return }
        setSending(true)
        setError(null)
        try {
            await moderateApplication(application.id, action, '')
            close()
        } catch (e) {
            setError(errorText(e))
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
                    {error && <p className="error">{error}</p>}
                </DialogContent>
                <DialogActions>
                    {application.status === 'SUBMITTED' && (
                        <Button color="success" disabled={sending} onClick={() => decide('approve')}>
                            Утвердить
                        </Button>
                    )}
                    <Button onClick={onClose}>Закрыть</Button>
                </DialogActions>
            </>)}
        </Dialog>
    )
}
