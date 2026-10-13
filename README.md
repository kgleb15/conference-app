# conference-app

Бэкенд системы подачи заявок на конференцию

Запуск: `./gradlew bootRun`;
сервер на `http://localhost:8080`

## Основные сведения

- Все тела запросов и ответов - JSON.
- Кто делает запрос, определяется заголовком `Person-Id: <id человека>`. 
- Дедлайн T1 задаётся в `application.properties`. После него подача, редактирование и отзыв заявки возвращают `403`.
- Ошибки валидации - `400`.
- У ответов `401`, `403`, `404`, `409` тела нет

## Запросы

### Участник

**POST /persons** — регистрация участника

Тело: { "fullName": "...", "groupName": "...", "email": "...", "phone": "..." }
Ответ `201`: данные человека (среди них id для последующей отправки в `Person-Id`).
Роль всегда выдается PARTICIPANT

**GET /applications** - список своих заявок
`200` - массив заявок (может быть пустым)

**GET /applications/{id}** - одна своя заявка
`200` - заявка; `404` - нет такой или она чужая.

**POST /applications** - подать заявку
Тело: { "title": "...", "thesisText": "..." } (оба поля обязательны).
`201`- созданная заявка; `400` - поля некорректны; `403` - дедлайн прошёл.

**PUT /applications/{id}** - изменить заявку (заменяет title и thesisText целиком).
`200` - обновлённая заявка; `400` - поля некорректны; `403` - дедлайн прошёл; `404` - нет или чужая;
`409` - заявка уже отозвана, редактировать нельзя.

**PATCH /applications/{id}/withdraw** - отозвать заявку.
`200` - заявка со статусом WITHDRAWN; `403` - дедлайн прошёл; `404` - нет или чужая.
Повторный отзыв тоже вернёт `200`

### Организатор

**GET /organizer/applications** — заявки всех участников (с данными автора)
Необязательные фильтры:
- `status=SUBMITTED` или `status=WITHDRAWN`
- `edited=true` - только изменённые, `edited=false` - только не менявшиеся

`200` - массив заявок; `403` - запрос сделал не организатор.

## Формат заявки

```json
{
  "id": "uuid",
  "title": "Название",
  "thesisText": "Текст тезисов",
  "status": "SUBMITTED",
  "createdAt": "...",
  "updatedAt": "...",
  "editCount": 0,
  "lastEditedAt": null,
  "withdrawnAt": null,
  "owner": { "id": "...", "fullName": "...", "groupName": "...", "email": "...", "phone": "...", "role": "PARTICIPANT" }
}
```

## Основные файлы в проекте

- `controllers/PersonController.java` - `POST /persons`.
- `controllers/ApplicationController.java` - запросы участника к заявкам, перевод результата сервиса в HTTP-код.
- `controllers/OrganizerController.java` - `GET /organizer/applications` с фильтрами.
- `services/ApplicationService.java` - вся логика заявок: права, дедлайн, подача/правка/отзыв, фильтры организатора.
- `services/PersonService.java` - регистрация участника.
- `model/Application.java` - сущность заявки, методы `update` и `withdraw`.
- `model/Person.java` - сущность человека.
- `config/ConferenceProperties.java` - чтение дедлайна T1 из настроек.
- `repositories/ApplicationRepository.java` - интерфейс хранилища заявок.
- `repositories/PersonRepository.java` - интерфейс хранилища людей.
- `repositories/InMemoryApplicationRepository.java` — хранение заявок в памяти.
- `repositories/InMemoryPersonRepository.java` — хранение людей в памяти
