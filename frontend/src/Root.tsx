import type { ReactElement } from 'react'
import App from '../src/pages/ModerationPage'
import Header from '../src/components/Header'
import HomePage from '../src/pages/HomePage'
import LoginPage from '../src/pages/LoginPage'
import RegisterPage from '../src/pages/RegisterPage'
import SubmitPage from '../src/pages/SubmitPage'
import MyApplicationsPage from '../src/pages/MyApplicationsPage'
import ExpertPage from '../src/pages/ExpertPage'
import AddExpertPage from '../src/pages//AddExpertPage'
import { getPerson, homePath } from '../src/api/auth'

function redirect(path: string) {
    window.location.replace(path)
    return null
}

export default function Root() {
    const path = window.location.pathname
    const person = getPerson()
    let page: ReactElement | null = null

    if (path === '/' || path === '/login' || path === '/register') {
        // страницы для тех, кто ещё не вошёл
        if (person) {
            return redirect(homePath(person.role))
        }
        if (path === '/') page = <HomePage />
        else if (path === '/login') page = <LoginPage />
        else page = <RegisterPage />
    } else if (person === null) {
        return redirect('/login')
    } else if (path === '/my' && person.role === 'PARTICIPANT') {
        page = <MyApplicationsPage />
    } else if (path === '/submit' && person.role === 'PARTICIPANT') {
        page = <SubmitPage />
    } else if (path === '/moderation' && person.role === 'ORGANIZER') {
        page = <App />
    } else if (path === '/add-expert' && person.role === 'ORGANIZER') {
        page = <AddExpertPage />
    } else if (path === '/review' && person.role === 'EXPERT') {
        page = <ExpertPage />
    } else {
        // Чужая или несуществующая страница
        return redirect(homePath(person.role))
    }

    return (<>
        <Header />
        {page}
    </>)
}
