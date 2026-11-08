import { Button } from '@mui/material'
import App from './App'
import LoginPage from './LoginPage'
import SubmitPage from './SubmitPage'
import { getAuthHeader, logout } from './auth'

export default function Root() {
    const path = window.location.pathname

    if (path === '/login') {
        return <LoginPage />
    }
    // Без входа доступна только страница входа
    if (getAuthHeader() === '') {
        window.location.replace('/login')
        return null
    }

    return (<>
        <div className="person">
            <Button variant="outlined" onClick={() => { logout(); window.location.href = '/login' }}>
                Выйти
            </Button>
        </div>
        {path === '/submit' ? <SubmitPage /> : <App />}
    </>)
}
