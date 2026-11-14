import { Outlet } from 'react-router'
import { getPerson } from '../api/auth'
import Footer from './Footer'
import Header from './Header'
import Sidebar from './Sidebar'

export default function Layout() {
    const person = getPerson()

    return (
        <div className={person ? 'layout with-sidebar' : 'layout'}>
            <Header />
            <div className="layout-body">
                {person && <Sidebar role={person.role} />}
                <main className="layout-main"><Outlet /></main>
            </div>
            <Footer />
        </div>
    )
}
