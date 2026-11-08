const KEY = 'authHeader'

/* Запоминает данные входа на время работы вкладки */
export function setCredentials(email: string, password: string): void {
    sessionStorage.setItem(KEY, 'Basic ' + btoa(`${email.trim()}:${password}`))
}

/* Значение заголовка Authorization */
export function getAuthHeader(): string {
    return sessionStorage.getItem(KEY) ?? ''
}

export function logout(): void {
    sessionStorage.removeItem(KEY)
}
