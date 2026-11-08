import { useState } from 'react'
import { Button, MenuItem, TextField } from '@mui/material'
import { ApiError, submitApplication } from './api'
import type { Application } from './types'
import './App.css'

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Заполните все поля'
    }
    if (e instanceof ApiError && e.status === 401) {
        return 'Нужно войти заново'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Дата или роль некорректны'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Превышен лимит заявок'
    }
    return 'Не удалось отправить заявку'
}

const EMPTY_FORM = {
    title: '', annotation: '', thesisText: '', groupName: '',
    applicantCategory: 'STUDENT', fundingType: 'BUDGET',
}

export default function SubmitPage() {
    const [form, setForm] = useState(EMPTY_FORM)
    const [error, setError] = useState<string | null>(null)
    const [sent, setSent] = useState(false)

    // Обновляет одно поле формы
    function change(field: string, value: string) {
        setForm({ ...form, [field]: value })
    }

    const canSend = form.title.trim() !== '' && form.annotation.trim() !== ''
        && form.thesisText.trim() !== '' && form.groupName.trim() !== ''

    async function send() {
        setError(null)
        setSent(false)
        try {
            await submitApplication(form as Partial<Application>)
            setForm(EMPTY_FORM)
            setSent(true)
        } catch (e) {
            setError(errorText(e))
        }
    }

    return (
        <div className="page form-page">
            <h1>Подача заявки</h1>

            <div className="form">
                <TextField label="Название" value={form.title}
                    onChange={(e) => change('title', e.target.value)} />
                <TextField label="Аннотация" multiline minRows={3} value={form.annotation}
                    onChange={(e) => change('annotation', e.target.value)} />
                <TextField label="Текст тезисов" multiline minRows={8} value={form.thesisText}
                    onChange={(e) => change('thesisText', e.target.value)} />
                <TextField label="Группа" size="small" value={form.groupName}
                    onChange={(e) => change('groupName', e.target.value)} />
                <TextField select label="Категория" size="small" value={form.applicantCategory}
                    onChange={(e) => change('applicantCategory', e.target.value)}>
                    <MenuItem value="STUDENT">Студент</MenuItem>
                    <MenuItem value="COLLEGE_STUDENT">Студент колледжа</MenuItem>
                    <MenuItem value="SCHOOLCHILD">Школьник</MenuItem>
                </TextField>
                <TextField select label="Финансирование" size="small" value={form.fundingType}
                    onChange={(e) => change('fundingType', e.target.value)}>
                    <MenuItem value="BUDGET">Бюджет</MenuItem>
                    <MenuItem value="CONTRACT">Контракт</MenuItem>
                </TextField>

                <Button variant="contained" disabled={!canSend} onClick={send}>Отправить</Button>

                {sent && <p>Заявка отправлена</p>}
                {error && <p className="error">{error}</p>}
            </div>
        </div>
    )
}
