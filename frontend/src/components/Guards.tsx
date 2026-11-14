import type { ReactElement } from 'react'
import { Navigate } from 'react-router'
import { getPerson, homePath } from '../api/auth'
import type { Person } from '../common/types'

/* Страница только для вошедших с нужной ролью */
export function RoleRoute({ role, children }: { role: Person['role'], children: ReactElement }) {
    const person = getPerson()
    if (person === null) {
        return <Navigate to="/login" replace />
    }
    if (person.role !== role) {
        return <Navigate to={homePath(person.role)} replace /> // с чужой страницы переход на свою
    }
    return children
}

/* Страница только для тех, кто ещё не вошёл */
export function GuestRoute({ children }: { children: ReactElement }) {
    const person = getPerson()
    if (person !== null) {
        return <Navigate to={homePath(person.role)} replace />
    }
    return children
}
