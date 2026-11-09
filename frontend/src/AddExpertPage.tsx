import { useState } from 'react'
import { Button, TextField } from '@mui/material'
import { ApiError, registerExpert } from './api'
import './App.css'

const EMPTY_FORM = { fullName: '', email: '', phone: '', password: '' }

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Некорректные поля'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Пользователь с таким email уже есть'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Создавать экспертов может только организатор'
    }
    return 'Не удалось создать эксперта'
}

export default function AddExpertPage() {
    const [form, setForm] = useState(EMPTY_FORM)
    const [error, setError] = useState<string | null>(null)
    const [created, setCreated] = useState<string | null>(null) // email созданного эксперта

    // Обновляет одно поле формы
    function change(field: string, value: string) {
        setForm({ ...form, [field]: value })
    }

    const canSend = form.fullName.trim() !== '' && form.email.trim() !== ''
        && form.phone.trim() !== '' && form.password.length >= 8

    async function send() {
        setError(null)
        setCreated(null)
        try {
            await registerExpert(form)
            setCreated(form.email)
            setForm(EMPTY_FORM)
        } catch (e) {
            setError(errorText(e))
        }
    }

    return (
        <div className="page form-page">
            <h1>Добавление эксперта</h1>
            <div className="form">
                <TextField label="ФИО" size="small" value={form.fullName}
                    onChange={(e) => change('fullName', e.target.value)} />
                <TextField label="Email" size="small" value={form.email}
                    onChange={(e) => change('email', e.target.value)} />
                <TextField label="Телефон" size="small" value={form.phone}
                    onChange={(e) => change('phone', e.target.value)} />
                <TextField label="Пароль" size="small" value={form.password}
                    helperText="Не короче 8 символов"
                    onChange={(e) => change('password', e.target.value)} />
                <Button variant="contained" disabled={!canSend} onClick={send}>Создать эксперта</Button>

                {created && <p>Эксперт {created} создан</p>}
                {error && <p className="error">{error}</p>}
            </div>
        </div>
    )
}
