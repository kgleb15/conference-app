import { useState } from 'react'
import { Outlet } from 'react-router'
import { getPerson } from '../api/auth'
import Footer from './Footer'
import Header from './Header'
import Sidebar from './Sidebar'
import './Layout.css'

export default function Layout() {
    const person = getPerson()
    const [sidebarOpen, setSidebarOpen] = useState(window.matchMedia('(min-width: 801px)').matches)

    return (
        <div className={person && sidebarOpen ? 'layout with-sidebar' : 'layout'}>
            <Header onToggleSidebar={person ? () => setSidebarOpen(!sidebarOpen) : undefined} />
            <div className="layout-body">
                {person && sidebarOpen && <Sidebar role={person.role} />}
                <main className="layout-main"><Outlet /></main>
            </div>
            <Footer />
        </div>
    )
}
