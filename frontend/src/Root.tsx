import { Button } from '@mui/material'
import App from './App'
import LoginPage from './LoginPage'
import SubmitPage from './SubmitPage'
import { getPerson, homePath, logout } from './auth'

export default function Root() {
    const path = window.location.pathname
    const person = getPerson()

    if (path === '/login') {
        return <LoginPage />
    }
    if (person === null) {
        window.location.replace('/login')
        return null
    }

    // Каждая страница доступна только своей роли
    let page = null
    if (path === '/submit' && person.role === 'PARTICIPANT') {
        page = <SubmitPage />
    } else if (path === '/moderation' && person.role === 'ORGANIZER') {
        page = <App />
    } else if (path === '/review' && person.role === 'EXPERT') {
        page = <h1 className="page">Раздел эксперта в разработке</h1>
    }
    if (page === null) {
        window.location.replace(homePath(person.role))
        return null
    }

    return (<>
        <div className="person">
            <Button variant="outlined" onClick={() => { logout(); window.location.href = '/login' }}>
                Выйти
            </Button>
        </div>
        {page}
    </>)
}
