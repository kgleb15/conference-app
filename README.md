# conference-app

Система подачи заявок на конференцию



Запуск (бэк. В папке backend): 

запуск:`./gradlew bootRun`;

сервер на `http://localhost:8080`;

тесты: `./gradlew test`.



Запуск (фронт. В папке frontend): 

установка зависимостей:`npm install`

запуск: `npm run dev`;

сервер на `http://localhost:5173`;

## Основные сведения

- Все тела запросов и ответов - JSON.
- Кто делает запрос, определяется заголовком `Person-Id: <id человека>`.
- Дедлайны (задаются в `application.properties`): 

T1 . До него участнику можно подавать, редактировать и отзывать заявку. 

T2 . До него участнику можно дорабатывать заявку, которую организатор вернул.

После дедлайна подача, правка и отзыв возвращают `403`.

- Ошибки валидации - `400`.
- У ответов `401`, `403`, `404`, `409` тела нет

## Статусы заявки

`SUBMITTED` - подана, ждёт решения организатора;

`WITHDRAWN` - отозвана участником;

`APPROVED` - утверждена организатором;

`REJECTED` - отклонена организатором;

`REVISION_REQUESTED` - отправлена на доработку.

## !!! Переключение хранилища: память - PostgreSQL

1) По умолчанию данные хранятся в памяти, база данных не нужна (профиль `default`, настройки в `application-default.properties`).

2) Чтобы работать через PostgreSQL, запустите бэкенд с профилем db: `./gradlew bootRun --args="--spring.profiles.active=db"`. Перед этим поднимите PostgreSQL на `localhost:5432` и создайте базу `conference_app`
   Логин и пароль (`postgres` / `12345`) заданы в файле `application-db.properties`, при необходимости поменяйте.

## Запросы

### Участник

**POST /persons** - регистрация участника

Тело: { "fullName": "...", "groupName": "...", "email": "...", "phone": "..." }
Ответ `201`: данные человека (среди них id для последующей отправки в `Person-Id`).
Роль всегда выдается PARTICIPANT. `400` - поля некорректны.

**GET /applications** - список своих заявок
`200` - массив заявок (может быть пустым)

**GET /applications/{id}** - одна своя заявка
`200` - заявка; `404` - нет такой заявки или она чужая.

**POST /applications** - подать заявку
Тело: { "title": "...", "thesisText": "..." }
`201`- созданная заявка; `400` - поля некорректны; `403` - дедлайн прошёл или не участник;  `409` - уже подано максимальное число заявок.

**PUT /applications/{id}** - изменить заявку (заменяет title и thesisText целиком).
`200` - обновлённая заявка; `400` - поля некорректны; `403` - дедлайн прошёл или не участник; `404` - нет или чужая; `409` - заявка отозвана или по ней уже принято решение.

**PATCH /applications/{id}/withdraw** - отозвать заявку.
`200` - заявка со статусом WITHDRAWN; `403` - дедлайн прошёл или не участник; `404` - нет или чужая; `409` - по заявке уже принято решение (утверждена или отклонена).
Повторный отзыв тоже вернёт `200`

### Организатор

**POST /persons/organizer** - регистрация организатора (аналогично с участником)

**GET /organizer/applications** - заявки всех участников (с данными автора)
Необязательные фильтры:

- `status` - `SUBMITTED`, `WITHDRAWN`, `APPROVED`, `REJECTED` или `REVISION_REQUESTED`

- `edited=true` - только изменённые, `edited=false` - только не менявшиеся

`200` - массив заявок; `403` - не организатор.

**PATCH /organizer/applications/{id}/approve** - утвердить заявку. 

**PATCH /organizer/applications/{id}/reject** - отклонить заявку. 

**PATCH /organizer/applications/{id}/request-revision** - отправить на доработку. 

Для `reject` и `request-revision` тело: { "comment": "..." } (комментарий обязателен). Решение возможно только по заявке в статусе `SUBMITTED`. `200` - заявка с новым статусом; `400` - нет комментария; `401` - пользователь не найден; `403` - не организатор; `404` - нет такой заявки; `409` - решение уже нельзя принять (заявка не SUBMITTED).

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
  "owner": {
    "id": "...",
    "fullName": "...",
    "groupName": "...",
    "email": "...",
    "phone": "...",
    "role": "PARTICIPANT"
  },
  "moderationComment": null,
  "decidedAt": null,
  "decidedByName": null
}
```

## Фронтенд

- `http://localhost:5173/` - панель организатора
- `http://localhost:5173/submit` - отдельная страница подачи заявки

## Основные файлы в проекте (бэкенд)

- `controllers/PersonController.java` - `POST /persons`, `POST /persons/organizer`.
- `controllers/ApplicationController.java` - запросы участника к заявкам, перевод результата сервиса в HTTP-код.
- `controllers/OrganizerController.java` - `GET /organizer/applications` с фильтрами.
- `services/ApplicationService.java` - логика заявок: права, дедлайн, подача/правка/отзыв, фильтры организатора.
- `services/PersonService.java` - регистрация участника и организатора.
- `model/Application.java` - сущность заявки, методы `update` и `withdraw`...
- `model/Person.java` - сущность человека.
- `config/ConferenceProperties.java` - чтение дедлайна T1 из настроек.
- `repositories/ApplicationRepository.java` - интерфейс хранилища заявок.
- `repositories/PersonRepository.java` - интерфейс хранилища людей.
- `repositories/InMemoryApplicationRepository.java` - хранение заявок в памяти.
- `repositories/InMemoryPersonRepository.java` - хранение людей в памяти
