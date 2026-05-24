# Diploma

Веб-приложение для совместного выбора ресторана: комнаты, гео- и кухонные фильтры, каталог заведений (импорт из KudaGo), 2 этапа голосования,  регистрация/вход, история комнат.

## Модули

| Путь | Назначение |
|------|------------|
| **`backend/app`** | Основной Spring Boot API (REST), Flyway-миграции, подключение модулей `auth`, `voting`, `history`. |
| **`backend/auth`** | JWT и доменная аутентификация |
| **`backend/voting`** | Домен голосования: комнаты, участники, этапы, каталог ресторанов, фильтры |
| **`backend/history`** | Чтение агрегированной истории комнат для API |
| **`backend/kudago-import-service`** | Отдельное приложение: импорт/актуализация каталога из [KudaGo](https://kudago.com/) в ту же БД; ручной `POST /internal/kudago/import` (токен), опционально план по cron. |
| **`frontend`** | React (Vite): UI |

## Запуск в Docker

1. В корне репозитория: скопировать **`cp .env.example .env`**, задать **`DB_PASSWORD`**, **`JWT_SECRET`** (Base64, см. комментарии в `.env.example`).
2. Для HTTPS у фронта в контейнере (опционально): `./scripts/gen-docker-ssl-mkcert.sh` — положит сертификаты в **`docker/ssl/`**.
3. Сборка и запуск:

   ```bash
   docker compose build
   docker compose up -d
   ```

4. По умолчанию: **frontend** — из `.env` (`HTTP_PUBLISH_PORT` / `HTTPS_PUBLISH_PORT`, обычно 80/443), **backend** — `8080`, **kudago-import** — `8081`, **Postgres** — `5432`.

   Таймауты этапов голосования (**`VOTING_STAGE_ONE_TIMEOUT_SECONDS`**, **`VOTING_STAGE_TWO_TIMEOUT_SECONDS`**) backend в Docker читает из **`.env`** (`env_file`). После смены значений: `docker compose up -d --force-recreate backend` и **новый** старт этапа в комнате.

Ручной импорт каталога (если поднят **kudago-import**):

```bash
curl -sS -X POST "http://127.0.0.1:${KUDAGO_IMPORT_PUBLISH_PORT:-8081}/internal/kudago/import" \
  -H "X-Import-Token: ${DIPLOMA_KUDAGOIMPORT_HTTPIMPORTTOKEN:-dev-kudago-import-diploma-local}" \
  -H "Content-Type: application/json"
```

Чтобы **`curl`** подхватил токен и порты из `.env` (а не дефолты в строке выше), из **корня репозитория** в том же терминале:

```bash
set -a && source .env && set +a
```

Так переменные из файла попадут в окружение процесса.

## Локальный запуск

Нужны **Java 17**, **Node.js**, **PostgreSQL** (удобно поднять только БД: `docker compose up -d postgres` из корня при готовом `.env`).

### Backend (основной API)

Из каталога **`backend/`**:

```bash
export DB_HOST=localhost DB_PORT=5432 DB_NAME=diploma DB_USER=diploma DB_PASSWORD='<как в .env>'
export JWT_SECRET='<Base64 как в .env>'
./gradlew :app:bootRun
```

API по умолчанию: **http://localhost:8080**.

### Сервис импорта KudaGo (опционально)

Там же, из **`backend/`** (можно положить переменные в **`backend/.env`** — подхватывается dotenv в dev):

```bash
./gradlew :kudago-import-service:bootRun
```

Сервис слушает **http://localhost:8081** (см. `server.port` в `kudago-import-service/.../application.properties`). Импорт: тот же `curl` на порт **8081**.

### Frontend

Из **`frontend/`**:

```bash
npm install
npm run dev
```
