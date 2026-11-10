import { useState } from 'react'
import { Button, TextField } from '@mui/material'
import { ApiError, login, register } from '../api/api'
import { homePath } from '../api/auth'
import '../styles/App.css'

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Проверьте поля: нужен корректный email, пароль не короче 8 символов'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Участник с таким email уже зарегистрирован'
    }
    return 'Не удалось зарегистрироваться'
}

export default function RegisterPage() {
    const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '' })
    const [error, setError] = useState<string | null>(null)

    // Обновляет одно поле формы
    function change(field: string, value: string) {
        setForm({ ...form, [field]: value })
    }

    const canSend = form.fullName.trim() !== '' && form.email.trim() !== ''
        && form.phone.trim() !== '' && form.password.length >= 8

    async function send() {
        setError(null)
        try {
            await register(form)
            const person = await login(form.email, form.password) // сразу входим
            window.location.href = homePath(person.role)
        } catch (e) {
            setError(errorText(e))
        }
    }

    return (
        <div className="page auth-page">
            <h1>Регистрация участника</h1>
            <div className="form auth-card">
                <TextField label="ФИО" size="small" value={form.fullName}
                    onChange={(e) => change('fullName', e.target.value)} />
                <TextField label="Email" size="small" value={form.email}
                    onChange={(e) => change('email', e.target.value)} />
                <TextField label="Телефон" size="small" value={form.phone}
                    onChange={(e) => change('phone', e.target.value)} />
                <TextField label="Пароль" type="password" size="small" value={form.password}
                    helperText="Не короче 8 символов"
                    onChange={(e) => change('password', e.target.value)} />
                <Button className="form_button" variant="contained" disabled={!canSend} onClick={send}>Зарегистрироваться</Button>
                {error && <p className="error">{error}</p>}
            </div>
        </div>
    )
}
