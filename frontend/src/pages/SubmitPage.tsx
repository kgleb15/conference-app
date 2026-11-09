import ApplicationForm from '../components/ApplicationForm'
import { submitApplication } from '../api/api'
import type { ApplicationFields } from '../common/types'
import '../styles/App.css'

const EMPTY_FORM: ApplicationFields = {
    title: '', annotation: '', thesisText: '', groupName: '',
    applicantCategory: 'STUDENT', fundingType: 'BUDGET',
}

export default function SubmitPage() {
    return (
        <div className="page form-page">
            <h1>Подача заявки</h1>
            <ApplicationForm initial={EMPTY_FORM} submitLabel="Отправить"
                onSend={async (data) => {
                    await submitApplication(data)
                    window.location.href = '/my'
                }} />
        </div>
    )
}
