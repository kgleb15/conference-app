import type { Application, ApplicationStatus, FundingType } from './types'

/* Виды статусов заявки (подписи) */
export const STATUS_LABELS: Record<ApplicationStatus, string> = {
    SUBMITTED: 'Подана',
    WITHDRAWN: 'Отозвана',
    APPROVED: 'Утверждена',
    REJECTED: 'Отклонена',
    REVISION_REQUESTED: 'На доработке',
    TRANSFER_PENDING: 'Передается в другое направление',
}


/* Подписи категорий участника */
export const CATEGORY_LABELS: Record<Application['applicantCategory'], string> = {
    STUDENT: 'Студент',
    COLLEGE_STUDENT: 'Студент колледжа',
    SCHOOLCHILD: 'Школьник',
}

/* Подписи форм обучения */
export const FUNDING_LABELS: Record<FundingType, string> = {
    BUDGET: 'Бюджет',
    CONTRACT: 'Контракт',
}

