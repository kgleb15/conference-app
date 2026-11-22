import ApplicationForm from '../components/ApplicationForm'
import { submitApplication } from '../api/api'
import { EMPTY_FORM } from '../common/applicationFields'
import { useNavigate } from 'react-router'
import '../styles/App.css'

export default function SubmitPage() {
    const navigate = useNavigate()

    return (
        <div className="page form-page">
            <h1>Подача заявки</h1>
            <ApplicationForm initial={EMPTY_FORM} submitLabel="Отправить"
                onSend={async (data) => {
                    await submitApplication(data)
                    navigate('/my')
                }} />
        </div>
    )
}
