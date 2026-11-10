import { useState } from 'react'
import { Button, TextField } from '@mui/material'
import { ApiError, login } from '../api/api'
import { homePath } from '../api/auth'
import '../styles/App.css'

export default function LoginPage() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState<string | null>(null)

    async function send() {
        setError(null)
        try {
            const person = await login(email, password)
            window.location.href = homePath(person.role)
        } catch (e) {
            setError(e instanceof ApiError && e.status === 401 ? 'Неверный email или пароль' : 'Не удалось войти')
        }
    }

    return (
        <div className="page auth-page">
            <h1>Вход</h1>
            <div className="form auth-card">
                <TextField label="Email" size="small" value={email}
                    onChange={(e) => setEmail(e.target.value)} />
                <TextField label="Пароль" type="password" size="small" value={password}
                    onChange={(e) => setPassword(e.target.value)} />
                <Button className="form_button" variant="contained" disabled={email.trim() === '' || password === ''}
                    onClick={send}>Войти</Button>
                {error && <p className="error">{error}</p>}
            </div>
        </div>
    )
}
