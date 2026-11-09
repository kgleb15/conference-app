/* Дата и время для таблиц ('--', если даты нет) */
export function formatDate(iso: string | null): string {
    return iso ? new Date(iso).toLocaleString() : '--'
}
