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
import Footer from './components/Footer'
import './styles/App.css'
import { getPerson, homePath } from '../src/api/auth'

function redirect(path: string) {
    window.location.replace(path)
    return null
}

export default function Root() {
    const path = window.location.pathname
    const person = getPerson()
    let page: ReactElement

    if (path === '/') {
        page = <HomePage />
    } else if (path === '/login' || path === '/register') {
        // Страницы для тех, кто ещё не вошёл
        if (person) {
            return redirect(homePath(person.role))
        }
        page = path === '/login' ? <LoginPage /> : <RegisterPage />
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
        <div className="layout">
            <Header />
            <main className="layout-main">{page}</main>
            <Footer />
        </div>
    </>)
}
