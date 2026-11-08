import { useState } from 'react'
import { Button, TextField } from '@mui/material'
import { ApiError, login } from './api'
import './App.css'

export default function LoginPage() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState<string | null>(null)

    async function send() {
        setError(null)
        try {
            await login(email, password)
            window.location.href = '/'
        } catch (e) {
            setError(e instanceof ApiError && e.status === 401 ? 'Неверный email или пароль' : 'Не удалось войти')
        }
    }

    return (
        <div className="page form-page">
            <h1>Вход</h1>
            <div className="form">
                <TextField label="Email" size="small" value={email}
                    onChange={(e) => setEmail(e.target.value)} />
                <TextField label="Пароль" type="password" size="small" value={password}
                    onChange={(e) => setPassword(e.target.value)} />
                <Button variant="contained" disabled={email.trim() === '' || password === ''}
                    onClick={send}>Войти</Button>
                {error && <p className="error">{error}</p>}
            </div>
        </div>
    )
}
