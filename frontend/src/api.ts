import type { Application, ApplicationStatus } from './types'
import { getAuthHeader, logout, setCredentials } from './auth'

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
    const res = await fetch(path, {
        method,
        headers: { 'Authorization': getAuthHeader(), 'Content-Type': 'application/json' },
        body: body === undefined ? undefined : JSON.stringify(body),
    })
    if (!res.ok) {
        throw new ApiError(res.status)
    }
    return res.json()
}

/* Вход: запоминаем данные и проверяем их запросом, доступным любой роли */
export async function login(email: string, password: string): Promise<void> {
    try {
        setCredentials(email, password)
        await request('/applications')
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

export async function submitApplication(data: Partial<Application>): Promise<void> {
    await request('/applications', 'POST', data)
}
