import { useState } from 'react'
import { Button, TextField } from '@mui/material'
import { ApiError, submitApplication } from './api'
import './App.css'

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Заполните название и текст тезисов'
    }
    if (e instanceof ApiError && e.status === 401) {
        return 'Участник с таким id не найден'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Дата некорректна или пользователь не участник'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Превышен лимит заявок'
    }
    return 'Не удалось отправить заявку'
}

export default function SubmitPage() {
    const [personId, setPersonId] = useState('')
    const [title, setTitle] = useState('')
    const [thesisText, setThesisText] = useState('')
    const [error, setError] = useState<string | null>(null)

    const canSend = personId.trim() !== '' && title.trim() !== '' && thesisText.trim() !== ''

    async function send() {
        setError(null)
        try {
            await submitApplication(personId, title, thesisText)
            setTitle('')
            setThesisText('')
        } catch (e) {
            setError(errorText(e))
        }
    }

    return (
        <div className="page form-page">
            <h1>Подача заявки</h1>

            <div className="form">
                <TextField label="Id участника" size="small" value={personId}
                    onChange={(e) => setPersonId(e.target.value)} />
                <TextField label="Название" value={title}
                    onChange={(e) => setTitle(e.target.value)} />
                <TextField label="Текст тезисов" multiline minRows={8} value={thesisText}
                    onChange={(e) => setThesisText(e.target.value)} />

                <Button variant="contained" disabled={!canSend} onClick={send}>Отправить</Button>

                {error && <p className="error">{error}</p>}
            </div>
        </div>
    )
}
