import type { ApplicationStatus } from './types'

/* Виды статусов заявки (подписи) */
export const STATUS_LABELS: Record<ApplicationStatus, string> = {
    SUBMITTED: 'Подана',
    WITHDRAWN: 'Отозвана',
    APPROVED: 'Утверждена',
    REJECTED: 'Отклонена',
    REVISION_REQUESTED: 'На доработке',
}
