import type { Application, ApplicationStatus } from './types'
import { getPersonId } from './auth'

export type StatusFilter = 'ALL' | ApplicationStatus
export type EditedFilter = 'ALL' | 'true' | 'false'

/* Ошибка HTTP: хранит код ответа сервера */
export class ApiError extends Error {
    status: number

    constructor(status: number) {
        super(`HTTP ${status}`)
        this.status = status
    }
}

export async function fetchOrganizerApplications(status: StatusFilter, edited: EditedFilter,): Promise<Application[]> {

    // Строим строку с параметрами
    const params = new URLSearchParams()
    if (status !== 'ALL') {
        params.set('status', status);
    }
    if (edited !== 'ALL') {
        params.set('edited', edited);
    }

    const res = await fetch(`/organizer/applications?${params}`, {
        headers: { 'Person-Id': getPersonId() },
    }) // http запрос
    if (!res.ok) {
        throw new ApiError(res.status)
    }
    return res.json() // Читаем тело ответа и парсим json
}
