import { List, ListItemButton, ListItemText } from '@mui/material'
import type { Person } from '../common/types'
import './Sidebar.css'

type MenuItem = { title: string, path: string }

// Пункты меню для каждой роли
const MENU: Record<Person['role'], MenuItem[]> = {
    PARTICIPANT: [
        { title: 'Главная', path: '/' },
        { title: 'Подать заявку', path: '/submit' },
        { title: 'Мои заявки', path: '/my' },
    ],
    ORGANIZER: [
        { title: 'Главная', path: '/' },
        { title: 'Заявки', path: '/moderation' },
        { title: 'Добавить эксперта', path: '/add-expert' },
    ],
    EXPERT: [
        { title: 'Главная', path: '/' },
        { title: 'Заявки', path: '/review' },
    ],
}

export default function Sidebar({ role }: { role: Person['role'] }) {
    const currentPath = window.location.pathname

    return (
        <nav className="sidebar">
            <div className="sidebar_title">Меню:</div>
            <List disablePadding>
                {MENU[role].map((item) => (
                    <ListItemButton key={item.path} href={item.path} selected={currentPath === item.path}>
                        <ListItemText primary={item.title} />
                    </ListItemButton>
                ))}
            </List>
        </nav>
    )
}
