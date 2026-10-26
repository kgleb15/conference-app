const KEY = 'personId'

/* Возвращает сохранённый id пользователя (пустая строка, если не задан) */
export function getPersonId(): string {
    return localStorage.getItem(KEY) ?? ''
}

/* Сохраняет id пользователя */
export function setPersonId(id: string): void {
    localStorage.setItem(KEY, id.trim())
}
