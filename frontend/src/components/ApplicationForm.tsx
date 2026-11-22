import { useEffect, useState } from 'react'
import { Button, MenuItem, TextField } from '@mui/material'
import { ApiError, fetchDirections } from '../api/api'
import type { ApplicationFields, CoAuthorFields, Direction, FundingType, Supervisor } from '../common/types'

type Props = {
    initial: ApplicationFields // начальные значения полей
    submitLabel: string // подпись кнопки
    isEdit?: boolean // при редактировании направление и email соавтора менять нельзя
    onSend: (data: ApplicationFields) => Promise<void>
}

function errorText(e: unknown): string {
    if (e instanceof ApiError && e.status === 400) {
        return 'Заполните все поля. Соавтор должен быть другим зарегистрированным участником (укажите его email)'
    }
    if (e instanceof ApiError && e.status === 401) {
        return 'Нужно войти заново'
    }
    if (e instanceof ApiError && e.status === 403) {
        return 'Срок прошёл или у вас нет прав'
    }
    if (e instanceof ApiError && e.status === 409) {
        return 'Достигнут лимит заявок (у вас или у соавтора) в этом направлении, либо заявку уже нельзя менять'
    }
    return 'Не удалось сохранить заявку'
}

export default function ApplicationForm({ initial, submitLabel, isEdit = false, onSend }: Props) {
    const [form, setForm] = useState(initial)
    const [directions, setDirections] = useState<Direction[]>([])
    const [error, setError] = useState<string | null>(null)
    const [sending, setSending] = useState(false)

    useEffect(() => {
        fetchDirections().then(setDirections).catch(() => setError('Не удалось загрузить список направлений'))
    }, [])

    // Обновляет одно текстовое поле формы
    function change(field: 'title' | 'annotation' | 'thesisText' | 'groupName', value: string) {
        setForm({ ...form, [field]: value })
    }

    function changeCategory(value: string) {
        const schoolchild = value === 'SCHOOLCHILD'
        setForm({
            ...form,
            applicantCategory: value as ApplicationFields['applicantCategory'],
            fundingType: schoolchild ? 'BUDGET' : form.fundingType,
            coAuthor: form.coAuthor && schoolchild ? { ...form.coAuthor, fundingType: 'BUDGET' } : form.coAuthor,
        })
    }

    function changeSupervisor(index: number, field: keyof Supervisor, value: string) {
        setForm({
            ...form,
            supervisors: form.supervisors.map((s, i) => (i === index ? { ...s, [field]: value } : s)),
        })
    }

    function addSupervisor() {
        setForm({ ...form, supervisors: [...form.supervisors, { fullName: '', workplace: '' }] })
    }

    function removeSupervisor(index: number) {
        setForm({ ...form, supervisors: form.supervisors.filter((_, i) => i !== index) })
    }

    function changeCoAuthor(field: keyof CoAuthorFields, value: string) {
        if (form.coAuthor) {
            setForm({ ...form, coAuthor: { ...form.coAuthor, [field]: value } })
        }
    }

    function toggleCoAuthor() {
        setForm({
            ...form,
            coAuthor: form.coAuthor ? null : { email: '', groupName: '', fundingType: 'BUDGET' },
        })
    }

    const schoolchild = form.applicantCategory === 'SCHOOLCHILD'

    const canSend = form.title.trim() !== '' && form.annotation.trim() !== ''
        && form.thesisText.trim() !== '' && form.groupName.trim() !== ''
        && form.directionId !== null
        && form.supervisors.every((s) => s.fullName.trim() !== '' && s.workplace.trim() !== '')
        && (form.coAuthor === null
            || (form.coAuthor.email.trim() !== '' && form.coAuthor.groupName.trim() !== ''))

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
                directionId: form.directionId,
                supervisors: form.supervisors.map((s) => ({
                    fullName: s.fullName.trim(),
                    workplace: s.workplace.trim(),
                })),
                coAuthor: form.coAuthor && {
                    email: form.coAuthor.email.trim(),
                    groupName: form.coAuthor.groupName.trim(),
                    fundingType: form.coAuthor.fundingType,
                },
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

            <TextField select label="Направление" size="small" disabled={isEdit}
                value={directions.length > 0 ? (form.directionId ?? '') : ''}
                onChange={(e) => setForm({ ...form, directionId: Number(e.target.value) })}>
                {directions.map((d) => (
                    <MenuItem key={d.id} value={d.id}>{d.id}. {d.name}</MenuItem>
                ))}
            </TextField>

            <div className="form_row">
                <TextField select label="Категория" size="small" value={form.applicantCategory}
                    onChange={(e) => changeCategory(e.target.value)}>
                    <MenuItem value="STUDENT">Студент</MenuItem>
                    <MenuItem value="COLLEGE_STUDENT">Студент колледжа</MenuItem>
                    <MenuItem value="SCHOOLCHILD">Школьник</MenuItem>
                </TextField>
                <TextField label="Группа" size="small" value={form.groupName}
                    onChange={(e) => change('groupName', e.target.value)} />
                <TextField select label="Форма обучения" size="small" value={form.fundingType}
                    disabled={schoolchild}
                    onChange={(e) => setForm({ ...form, fundingType: e.target.value as FundingType })}>
                    <MenuItem value="BUDGET">Бюджет</MenuItem>
                    <MenuItem value="CONTRACT">Контракт</MenuItem>
                </TextField>
            </div>

            <p><b>Научные руководители</b> (от 1 до 2)</p>
            {form.supervisors.map((s, i) => (
                <div className="form_row" key={i}>
                    <TextField label="ФИО полностью" size="small" value={s.fullName}
                        onChange={(e) => changeSupervisor(i, 'fullName', e.target.value)} />
                    <TextField label="Место работы, должность" size="small" value={s.workplace}
                        onChange={(e) => changeSupervisor(i, 'workplace', e.target.value)} />
                    {i > 0 && <Button size="small" color="error" onClick={() => removeSupervisor(i)}>Убрать</Button>}
                </div>
            ))}
            {form.supervisors.length < 2 && (
                <Button size="small" onClick={addSupervisor}>Добавить научного руководителя</Button>
            )}

            {(!isEdit || form.coAuthor !== null) && <p><b>Соавтор</b> (необязательно)</p>}
            {form.coAuthor && (
                <div className="form_row">
                    <TextField label="Email соавтора (должен быть зарегистрирован)" size="small"
                        value={form.coAuthor.email} disabled={isEdit}
                        onChange={(e) => changeCoAuthor('email', e.target.value)} />
                    <TextField label="Группа соавтора" size="small" value={form.coAuthor.groupName}
                        onChange={(e) => changeCoAuthor('groupName', e.target.value)} />
                    <TextField select label="Форма обучения соавтора" size="small"
                        value={form.coAuthor.fundingType} disabled={schoolchild}
                        onChange={(e) => changeCoAuthor('fundingType', e.target.value)}>
                        <MenuItem value="BUDGET">Бюджет</MenuItem>
                        <MenuItem value="CONTRACT">Контракт</MenuItem>
                    </TextField>
                </div>
            )}
            {!isEdit && (
                <Button size="small" onClick={toggleCoAuthor}>
                    {form.coAuthor ? 'Убрать соавтора' : 'Добавить соавтора'}
                </Button>
            )}

            <Button className="form_button" variant="contained" disabled={!canSend || sending}
                onClick={send}>{submitLabel}</Button>

            {error && <p className="error">{error}</p>}
        </div>
    )
}
