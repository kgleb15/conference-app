export type ApplicationStatus = 'SUBMITTED' | 'WITHDRAWN' | 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED'
export type FundingType = 'BUDGET' | 'CONTRACT'

export interface Person {
  id: string
  fullName: string
  email: string
  phone: string | null
  role: 'PARTICIPANT' | 'ORGANIZER' | 'EXPERT'
  directionId: number | null
}

export interface Application {
  id: string
  title: string
  annotation: string
  thesisText: string
  applicantCategory: 'STUDENT' | 'COLLEGE_STUDENT' | 'SCHOOLCHILD'
  status: ApplicationStatus
  createdAt: string
  updatedAt: string
  editCount: number
  lastEditedAt: string | null
  withdrawnAt: string | null
  moderationComment: string | null
  decidedAt: string | null
  decidedByName: string | null
  directionId: number
  supervisors: Supervisor[]
  authors: Author[]
}

/* Соавтор в форме заявки */
export interface CoAuthorFields {
  email: string
  groupName: string
  fundingType: FundingType
}

/* Поля, которые участник заполняет в заявке */
export interface ApplicationFields {
  title: string
  annotation: string
  thesisText: string
  groupName: string
  applicantCategory: Application['applicantCategory']
  fundingType: FundingType
  directionId: number | null // null пока направление не выбрано
  supervisors: Supervisor[]
  coAuthor: CoAuthorFields | null
}

/* Заявка как её видит эксперт */
export interface ExpertApplication {
  id: string
  title: string
  annotation: string
  thesisText: string
  applicantCategory: Application['applicantCategory']
}

/* Направление конкурса */
export interface Direction {
  id: number
  name: string
}

/* Научный руководитель */
export interface Supervisor {
  fullName: string
  workplace: string
}

/* Автор заявки */
export interface Author {
  person: Person
  groupName: string
  fundingType: FundingType
}
