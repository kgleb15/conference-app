import type { Application } from './types'

export type StatusFilter = 'ALL' | 'SUBMITTED' | 'WITHDRAWN'
export type EditedFilter = 'ALL' | 'true' | 'false'

export async function fetchOrganizerApplications(status: StatusFilter, edited: EditedFilter,): Promise<Application[]> {
  
    // Строим строку с параметрами
    const params = new URLSearchParams()
    if (status !== 'ALL') { 
        params.set('status', status);
    }
    if (edited !== 'ALL') {
        params.set('edited', edited);
    }

    const res = await fetch(`/organizer/applications?${params}`) // http запрос
    return res.json() // Читаем тело ответа и парсим json
}
