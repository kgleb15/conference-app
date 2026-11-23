import { useState } from 'react'
import { Outlet } from 'react-router'
import { getPerson } from '../../api/auth.ts'
import Footer from '../Footer/Footer.tsx'
import Header from '../Header/Header.tsx'
import Sidebar from '../Sidebar/Sidebar.tsx'
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
