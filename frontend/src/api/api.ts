import type { Application, ApplicationFields, ApplicationStatus, ExpertApplication, Person } from '../common/types'
import { getAuthHeader, logout, savePerson, setCredentials } from './auth'

export type StatusFilter = 'ALL' | ApplicationStatus
export type EditedFilter = 'ALL' | 'true' | 'false'
export type DecisionAction = 'approve' | 'reject' | 'request-revision'

/* Ошибка HTTP: хранит код ответа сервера */
export class ApiError extends Error {
    status: number

    constructor(status: number) {
        super(`HTTP ${status}`)
        this.status = status
    }
}

/* Общий запрос: добавляет данные входа и проверяет код ответа */
async function request(path: string, method = 'GET', body?: unknown) {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    const auth = getAuthHeader()
    if (auth !== '') {
        headers['Authorization'] = auth
    }
    const res = await fetch(path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
    })
    if (!res.ok) {
        throw new ApiError(res.status)
    }
    return res.json()
}

/* Вход */
export async function login(email: string, password: string): Promise<Person> {
    try {
        setCredentials(email, password)
        const person: Person = await request('/persons/me')
        savePerson(person)
        return person
    } catch (e) {
        logout()
        throw e
    }
}

export function fetchOrganizerApplications(status: StatusFilter, edited: EditedFilter): Promise<Application[]> {
    const params = new URLSearchParams()
    if (status !== 'ALL') {
        params.set('status', status);
    }
    if (edited !== 'ALL') {
        params.set('edited', edited);
    }
    return request(`/organizer/applications?${params}`)
}

export async function moderateApplication(id: string, action: DecisionAction, comment: string): Promise<void> {
    await request(`/organizer/applications/${id}/${action}`, 'PATCH', { comment })
}

export async function submitApplication(data: ApplicationFields): Promise<void> {
    await request('/applications', 'POST', data)
}

export async function register(data: { fullName: string, email: string, phone: string, password: string }): Promise<void> {
    await request('/persons', 'POST', data)
}

export function fetchMyApplications(): Promise<Application[]> {
    return request('/applications')
}

export async function editApplication(id: string, data: ApplicationFields): Promise<void> {
    await request(`/applications/${id}`, 'PUT', data)
}

export async function withdrawApplication(id: string): Promise<void> {
    await request(`/applications/${id}/withdraw`, 'PATCH')
}

export function fetchExpertApplications(): Promise<ExpertApplication[]> {
    return request('/expert/applications')
}

export async function registerExpert(data: Parameters<typeof register>[0]): Promise<void> {
    await request('/persons/expert', 'POST', data)
}
