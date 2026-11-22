export type ApplicationStatus = 'SUBMITTED' | 'WITHDRAWN' | 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED'

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
  groupName: string
  applicantCategory: 'STUDENT' | 'COLLEGE_STUDENT' | 'SCHOOLCHILD'
  fundingType: 'BUDGET' | 'CONTRACT'
  status: ApplicationStatus
  createdAt: string
  updatedAt: string
  editCount: number
  lastEditedAt: string | null
  withdrawnAt: string | null
  owner: Person
  moderationComment: string | null
  decidedAt: string | null
  decidedByName: string | null
  directionId: number
  supervisors: Supervisor[]
  authors: Author[]
}

/* Поля, которые участник заполняет в заявке */
export type ApplicationFields = Pick<Application,
  'title' | 'annotation' | 'thesisText' | 'groupName' | 'applicantCategory' | 'fundingType'>

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
  fundingType: 'BUDGET' | 'CONTRACT'
}
