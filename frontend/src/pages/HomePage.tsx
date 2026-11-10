import { Button } from '@mui/material'
import Accordion from '../components/Accordion'
import './HomePage.css'
import { getPerson } from '../api/auth'

const FAQ = [
    {
        question: 'Кто может участвовать в конкурсе?',
        answer: 'Студенты ВолгГТУ, студенты колледжей и школьники Волгограда и области',
    },
    {
        question: 'Сколько авторов может быть у работы?',
        answer: 'Не более двух авторов и не более двух научных руководителей.',
    },
    {
        question: 'Сколько работ можно подать?',
        answer: 'Один человек может быть автором или соавтором не более чем двух работ в рамках одного направления конкурса.',
    },
    {
        question: 'Как проходит выступление?',
        answer: 'По принятой работе делается авторский доклад на 5-7 минут. Чертежи, графики и программные продукты демонстрируются при докладе.',
    },
]

export default function HomePage() {
    const person = getPerson()

    return (
        <div className="home">
            <h1>Конкурс научно-исследовательских работ ВолгГТУ</h1>
            <p className="home_lead">
                Конкурс проходит по 17 направлениям. Участвовать могут студенты ВолгГТУ, студенты колледжей
                и школьники Волгоградской области. Зарегистрируйтесь, подайте заявку на свою работу
            </p>
            <img className="home_image" src="/hero.jpg" alt="ВолгГТУ" />

            {!person && (
                <div className="home_cta">
                    <h2>Примите участие</h2>
                    <p>Войдите, если у вас уже есть аккаунт, или зарегистрируйтесь за минуту.</p>
                    <div className="home_cta_buttons">
                        <Button variant="contained" size="large" href="/login">Войти</Button>
                        <Button variant="outlined" size="large" href="/register">Зарегистрироваться</Button>
                    </div>
                </div>
            )}

            <h2>Частые вопросы</h2>
            <Accordion items={FAQ} />
        </div>
    )
}
