import { List, ListItemButton, ListItemText } from '@mui/material'
import type { Person } from '../../common/types.ts'
import { Link, useLocation } from 'react-router'
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
        { title: 'Добавить организатора', path: '/add-organizer' },
    ],
    EXPERT: [
        { title: 'Главная', path: '/' },
        { title: 'Заявки', path: '/review' },
    ],
}

export default function Sidebar({ role }: { role: Person['role'] }) {
    const { pathname } = useLocation()

    return (
        <nav className="sidebar">
            <div className="sidebar_title">Меню:</div>
            <List disablePadding>
                {MENU[role].map((item) => (
                    <ListItemButton key={item.path} component={Link} to={item.path} selected={pathname === item.path}>
                        <ListItemText primary={item.title} />
                    </ListItemButton>
                ))}
            </List>
        </nav>
    )
}
