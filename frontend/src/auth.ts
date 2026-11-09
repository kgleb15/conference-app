import type { Person } from './types'

const AUTH_KEY = 'authHeader'
const PERSON_KEY = 'person'

/* Запоминает данные входа на время работы вкладки */
export function setCredentials(email: string, password: string): void {
    sessionStorage.setItem(AUTH_KEY, 'Basic ' + btoa(`${email.trim()}:${password}`))
}

/* Значение заголовка Authorization */
export function getAuthHeader(): string {
    return sessionStorage.getItem(AUTH_KEY) ?? ''
}

export function savePerson(person: Person): void {
    sessionStorage.setItem(PERSON_KEY, JSON.stringify(person))
}

/* Возвращает того, кто вошёл */
export function getPerson(): Person | null {
    const text = sessionStorage.getItem(PERSON_KEY)
    return text ? JSON.parse(text) : null
}

export function logout(): void {
    sessionStorage.removeItem(AUTH_KEY)
    sessionStorage.removeItem(PERSON_KEY)
}

/* Страница, на которую попадает человек после входа */
export function homePath(role: Person['role']): string {
    if (role === 'ORGANIZER') return '/moderation'
    if (role === 'EXPERT') return '/review'
    return '/my'
}
