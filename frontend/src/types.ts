export type ApplicationStatus = 'SUBMITTED' | 'WITHDRAWN' | 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED'

export interface Person {
  id: string
  fullName: string
  groupName: string
  email: string
  phone: string
  role: 'PARTICIPANT' | 'ORGANIZER'
}

export interface Application {
  id: string
  title: string
  thesisText: string
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
