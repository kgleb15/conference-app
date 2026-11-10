import type { Application, ApplicationStatus } from './types'

/* Виды статусов заявки (подписи) */
export const STATUS_LABELS: Record<ApplicationStatus, string> = {
    SUBMITTED: 'Подана',
    WITHDRAWN: 'Отозвана',
    APPROVED: 'Утверждена',
    REJECTED: 'Отклонена',
    REVISION_REQUESTED: 'На доработке',
}


/* Подписи категорий участника */
export const CATEGORY_LABELS: Record<Application['applicantCategory'], string> = {
    STUDENT: 'Студент',
    COLLEGE_STUDENT: 'Студент колледжа',
    SCHOOLCHILD: 'Школьник',
}

/* Подписи форм обучения */
export const FUNDING_LABELS: Record<Application['fundingType'], string> = {
    BUDGET: 'Бюджет',
    CONTRACT: 'Контракт',
}

