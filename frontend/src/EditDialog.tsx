import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material'
import ApplicationForm from './ApplicationForm'
import { editApplication } from './api'
import type { Application } from './types'

type Props = {
    application: Application | null
    onClose: () => void
    onChanged: () => void
}

export default function EditDialog({ application, onClose, onChanged }: Props) {
    return (
        <Dialog open={application !== null} onClose={onClose} maxWidth="md" fullWidth>
            {application && (<>
                <DialogTitle>Редактирование заявки</DialogTitle>
                <DialogContent>
                    {application.moderationComment && (
                        <p><b>Комментарий организатора:</b> {application.moderationComment}</p>
                    )}
                    <div style={{ paddingTop: 8 }}>
                        <ApplicationForm initial={application} submitLabel="Сохранить"
                            onSend={async (data) => {
                                await editApplication(application.id, data)
                                onChanged()
                                onClose()
                            }} />
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onClose}>Закрыть</Button>
                </DialogActions>
            </>)}
        </Dialog>
    )
}
