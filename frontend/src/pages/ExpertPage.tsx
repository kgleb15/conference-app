import { useEffect, useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { ApiError, fetchExpertApplications } from '../api/api'
import type { ExpertApplication } from '../common/types'
import '../styles/App.css'

const CATEGORY_NAMES = {
    STUDENT: 'Студент',
    COLLEGE_STUDENT: 'Студент колледжа',
    SCHOOLCHILD: 'Школьник',
}

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 401) {
        return 'Нужно войти заново'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Раздел доступен только членам экспертной комиссии'
    }
    return 'Не удалось загрузить заявки'
}

export default function ExpertPage() {
    const [data, setData] = useState<ExpertApplication[] | null>(null)
    const [error, setError] = useState<string | null>(null)
    const [selected, setSelected] = useState<ExpertApplication | null>(null) // открытая заявка

    useEffect(() => {
        fetchExpertApplications()
            .then(setData)
            .catch((e) => setError(errorText(e)))
    }, [])

    return (
        <div className="page">
            <h1>Заявки для экспертизы</h1>

            {error && <p className="error">{error}</p>}
            {data === null && error === null && <p>Загрузка...</p>}
            {data && data.length === 0 && <p>Утверждённых заявок пока нет</p>}

            {data && data.length > 0 && (
                <table>
                    <thead>
                        <tr> <th>Название</th> <th>Категория</th> <th>Аннотация</th> </tr>
                    </thead>
                    <tbody>
                        {data.map((a) => (
                            <tr key={a.id} className="row" onClick={() => setSelected(a)}>
                                <td>{a.title}</td>
                                <td>{CATEGORY_NAMES[a.applicantCategory]}</td>
                                <td>{a.annotation}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            <Dialog open={selected !== null} onClose={() => setSelected(null)} maxWidth="md" fullWidth>
                {selected && (<>
                    <DialogTitle>{selected.title}</DialogTitle>
                    <DialogContent>
                        <p><b>Категория:</b> {CATEGORY_NAMES[selected.applicantCategory]}</p>
                        <p><b>Аннотация:</b> {selected.annotation}</p>
                        <p><b>Текст тезисов:</b></p>
                        <div style={{ whiteSpace: 'pre-wrap' }}>{selected.thesisText}</div>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setSelected(null)}>Закрыть</Button>
                    </DialogActions>
                </>)}
            </Dialog>
        </div>
    )
}
