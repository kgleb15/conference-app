import { useEffect, useState } from 'react'
import { Button, MenuItem, TextField } from '@mui/material'
import { ApiError, fetchDirections, registerOrganizer } from '../api/api'
import type { Direction } from '../common/types'
import '../styles/App.css'

const EMPTY_FORM = { fullName: '', email: '', password: '' }

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Некорректные поля'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Пользователь с таким email уже есть'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Создавать организаторов может только организатор'
    }
    return 'Не удалось создать организатора'
}

export default function AddOrganizerPage() {
    const [form, setForm] = useState(EMPTY_FORM)
    const [directionId, setDirectionId] = useState<number | null>(null)
    const [directions, setDirections] = useState<Direction[]>([])
    const [error, setError] = useState<string | null>(null)
    const [created, setCreated] = useState<string | null>(null) // email созданного организатора

    useEffect(() => {
        fetchDirections().then(setDirections)
            .catch(() => setError('Не удалось загрузить список направлений'))
    }, [])

    // Обновляет одно поле формы
    function change(field: string, value: string) {
        setForm({ ...form, [field]: value })
    }

    const canSend = form.fullName.trim() !== '' && form.email.trim() !== ''
        && form.password.length >= 8 && directionId !== null

    async function send() {
        if (directionId === null) { return }
        setError(null)
        setCreated(null)
        try {
            await registerOrganizer({ ...form, directionId })
            setCreated(form.email)
            setForm(EMPTY_FORM)
        } catch (e) {
            setError(errorText(e))
        }
    }

    return (
        <div className="page form-page">
            <h1>Добавление организатора</h1>
            <div className="form">
                <TextField label="ФИО" size="small" value={form.fullName}
                    onChange={(e) => change('fullName', e.target.value)} />
                <TextField label="Email" size="small" value={form.email}
                    onChange={(e) => change('email', e.target.value)} />
                <TextField label="Пароль" size="small" value={form.password}
                    helperText="Не короче 8 символов"
                    onChange={(e) => change('password', e.target.value)} />
                <TextField select label="Направление" size="small"
                    value={directions.length > 0 ? (directionId ?? '') : ''}
                    onChange={(e) => setDirectionId(Number(e.target.value))}>
                    {directions.map((d) => (
                        <MenuItem key={d.id} value={d.id}>{d.id}. {d.name}</MenuItem>
                    ))}
                </TextField>
                <Button variant="contained" disabled={!canSend} onClick={send}>Создать организатора</Button>

                {created && <p>Организатор {created} создан</p>}
                {error && <p className="error">{error}</p>}
            </div>
        </div>
    )
}
