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
  status: 'SUBMITTED' | 'WITHDRAWN'
  createdAt: string
  updatedAt: string
  editCount: number
  lastEditedAt: string | null
  withdrawnAt: string | null
  owner: Person
}
