import { Button } from '@mui/material'
import '../styles/App.css'

export default function HomePage() {
    return (
        <div className="page">
            <h1>Конкурс научно-исследовательских работ ВолгГТУ</h1>
            <p>
                Конкурс проходит по 17 направлениям. Участвовать могут студенты ВолгГТУ, студенты колледжей
                и школьники Волгоградской области. Зарегистрируйтесь, подайте заявку на свою работу
                и следите за её статусом в личном кабинете.
            </p>
            <img src="/hero.jpg" alt="ВолгГТУ" style={{ maxWidth: '100%' }} />
            <div style={{ display: 'flex', gap: 16, marginTop: 16 }}>
                <Button variant="contained" href="/login">Войти</Button>
                <Button variant="outlined" href="/register">Зарегистрироваться</Button>
            </div>
        </div>
    )
}
