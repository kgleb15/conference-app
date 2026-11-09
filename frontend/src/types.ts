export type ApplicationStatus = 'SUBMITTED' | 'WITHDRAWN' | 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED'

export interface Person {
  id: string
  fullName: string
  email: string
  phone: string
  role: 'PARTICIPANT' | 'ORGANIZER' | 'EXPERT'
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
}

/* Поля, которые участник заполняет в заявке */
export type ApplicationFields = Pick<Application,
  'title' | 'annotation' | 'thesisText' | 'groupName' | 'applicantCategory' | 'fundingType'>
