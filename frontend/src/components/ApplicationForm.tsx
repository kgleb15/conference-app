import { useState } from 'react'
import { Button, MenuItem, TextField } from '@mui/material'
import { ApiError } from '../api/api'
import type { ApplicationFields } from '../common/types'

type Props = {
    initial: ApplicationFields // начальные значения полей
    submitLabel: string // подпись кнопки
    onSend: (data: ApplicationFields) => Promise<void>
}

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Заполните все поля'
    }
    if (e instanceof ApiError && e.status === 401) {
        return 'Нужно войти заново'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Срок прошёл или у вас нет прав'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Достигнут лимит заявок или заявку уже нельзя менять'
    }
    return 'Не удалось сохранить заявку'
}

export default function ApplicationForm({ initial, submitLabel, onSend }: Props) {
    const [form, setForm] = useState(initial)
    const [error, setError] = useState<string | null>(null)
    const [sending, setSending] = useState(false)

    // Обновляет одно поле формы
    function change(field: keyof ApplicationFields, value: string) {
        setForm({ ...form, [field]: value })
    }


    function changeCategory(value: string) {
        setForm({
            ...form,
            applicantCategory: value as ApplicationFields['applicantCategory'],
            fundingType: value === 'SCHOOLCHILD' ? 'BUDGET' : form.fundingType,
        })
    }

    const canSend = form.title.trim() !== '' && form.annotation.trim() !== ''
        && form.thesisText.trim() !== '' && form.groupName.trim() !== ''

    async function send() {
        setSending(true)
        setError(null)
        try {
            await onSend({
                title: form.title.trim(),
                annotation: form.annotation.trim(),
                thesisText: form.thesisText.trim(),
                groupName: form.groupName.trim(),
                applicantCategory: form.applicantCategory,
                fundingType: form.fundingType,
            })
        } catch (e) {
            setError(errorText(e))
        }
        setSending(false)
    }

    return (
        <div className="form">
            <TextField label="Название" value={form.title}
                onChange={(e) => change('title', e.target.value)} />
            <TextField label="Аннотация" multiline minRows={3} value={form.annotation}
                onChange={(e) => change('annotation', e.target.value)} />
            <TextField label="Текст тезисов" multiline minRows={8} value={form.thesisText}
                onChange={(e) => change('thesisText', e.target.value)} />
            <div className="form_row">
                <TextField label="Группа" size="small" value={form.groupName}
                    onChange={(e) => change('groupName', e.target.value)} />
                <TextField select label="Направление" size="small" value="" disabled>
                    <MenuItem value="">Не выбрано</MenuItem>
                </TextField>
            </div>
            <div className="form_row">
                <TextField select label="Категория" size="small" value={form.applicantCategory}
                    onChange={(e) => changeCategory(e.target.value)}>
                    <MenuItem value="STUDENT">Студент</MenuItem>
                    <MenuItem value="COLLEGE_STUDENT">Студент колледжа</MenuItem>
                    <MenuItem value="SCHOOLCHILD">Школьник</MenuItem>
                </TextField>
                <TextField select label="Форма обучения" size="small" value={form.fundingType}
                    disabled={form.applicantCategory === 'SCHOOLCHILD'}
                    onChange={(e) => change('fundingType', e.target.value)}>
                    <MenuItem value="BUDGET">Бюджет</MenuItem>
                    <MenuItem value="CONTRACT">Контракт</MenuItem>
                </TextField>
            </div>

            <Button className="form_button" variant="contained" disabled={!canSend || sending}
                onClick={send}>{submitLabel}</Button>

            {error && <p className="error">{error}</p>}
        </div>
    )
}
