import { Button, Dialog, DialogContent, DialogTitle } from '@mui/material'
import { STATUS_LABELS } from './statuses'
import type { Application } from './types'

type Props = {
    application: Application | null
    onClose: () => void
}

export default function ApplicationDialog(props: Props) {
    const { application, onClose } = props

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
                </DialogContent>
                <Button onClick={onClose}>Закрыть</Button>
            </>)}
        </Dialog>
    )
}
