import type { Application, ApplicationFields } from './types'

/* Пустая форма подачи заявки */
export const EMPTY_FORM: ApplicationFields = {
    title: '', annotation: '', thesisText: '', groupName: '',
    applicantCategory: 'STUDENT', fundingType: 'BUDGET',
    directionId: null,
    supervisors: [{ fullName: '', workplace: '' }],
    coAuthor: null,
}

/* Переносит заявку с сервера в поля формы редактирования */
export function fieldsFromApplication(a: Application): ApplicationFields {
    const first = a.authors[0]
    const second = a.authors[1]
    return {
        title: a.title,
        annotation: a.annotation,
        thesisText: a.thesisText,
        groupName: first.groupName,
        applicantCategory: a.applicantCategory,
        fundingType: first.fundingType,
        directionId: a.directionId,
        supervisors: a.supervisors.map((s) => ({ ...s })),
        coAuthor: second
            ? { email: second.person.email, groupName: second.groupName, fundingType: second.fundingType }
            : null,
    }
}
