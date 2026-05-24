import type { FormEvent } from 'react'
import '../../styles/AuthPages.css'
import '../../styles/RoomEntryPage.css'

export type RoomEntryPageViewProps = {
  checkingActiveRoom: boolean
  notice: string | null
  createPassword: string
  joinCode: string
  joinPassword: string
  createError: string | null
  joinError: string | null
  createPending: boolean
  joinPending: boolean
  onCreatePasswordChange: (value: string) => void
  onJoinCodeChange: (value: string) => void
  onJoinPasswordChange: (value: string) => void
  onCreateSubmit: (e: FormEvent) => void
  onJoinSubmit: (e: FormEvent) => void
}

export function RoomEntryPageView({
  checkingActiveRoom,
  notice,
  createPassword,
  joinCode,
  joinPassword,
  createError,
  joinError,
  createPending,
  joinPending,
  onCreatePasswordChange,
  onJoinCodeChange,
  onJoinPasswordChange,
  onCreateSubmit,
  onJoinSubmit,
}: RoomEntryPageViewProps) {
  if (checkingActiveRoom) {
    return (
      <div className="room-entry-shell">
        <p className="room-entry__checking">Проверка активной комнаты…</p>
      </div>
    )
  }

  return (
    <div className="room-entry-shell">
      {notice && <p className="room-entry__notice">{notice}</p>}
      <div className="room-entry__intro">
        <h1 className="room-entry__title">Комната</h1>
        <p className="room-entry__lead">
          Создайте новую комнату или введите код и пароль, чтобы присоединиться к уже существующей.
        </p>
      </div>
      <div className="room-entry__grid">
        <div className="auth-panel room-entry__card">
          <div className="auth-page">
            <h2 className="auth-page__title room-entry__card-title">Создать комнату</h2>
            <p className="auth-page__lead">
              Задайте пароль комнаты. Код сгенерируется автоматически — на следующем экране вы сможете
              передать его участникам.
            </p>
            <form className="auth-form" onSubmit={onCreateSubmit}>
              <label className="auth-form__field">
                <span>Пароль комнаты</span>
                <input
                  type="password"
                  autoComplete="new-password"
                  minLength={4}
                  maxLength={255}
                  value={createPassword}
                  onChange={(e) => onCreatePasswordChange(e.target.value)}
                  required
                />
              </label>
              {createError && <p className="auth-form__error">{createError}</p>}
              <button type="submit" className="auth-form__submit" disabled={createPending}>
                {createPending ? 'Создание…' : 'Создать комнату'}
              </button>
            </form>
          </div>
        </div>

        <div className="auth-panel room-entry__card">
          <div className="auth-page">
            <h2 className="auth-page__title room-entry__card-title">Присоединиться</h2>
            <p className="auth-page__lead">Введите код комнаты и пароль, полученные от организатора.</p>
            <form className="auth-form" onSubmit={onJoinSubmit}>
              <label className="auth-form__field">
                <span>Код комнаты</span>
                <input
                  type="text"
                  autoComplete="off"
                  spellCheck={false}
                  minLength={1}
                  maxLength={32}
                  value={joinCode}
                  onChange={(e) => onJoinCodeChange(e.target.value.toUpperCase())}
                  required
                />
              </label>
              <label className="auth-form__field">
                <span>Пароль комнаты</span>
                <input
                  type="password"
                  autoComplete="new-password"
                  minLength={4}
                  maxLength={255}
                  value={joinPassword}
                  onChange={(e) => onJoinPasswordChange(e.target.value)}
                  required
                />
              </label>
              {joinError && <p className="auth-form__error">{joinError}</p>}
              <button type="submit" className="auth-form__submit" disabled={joinPending}>
                {joinPending ? 'Вход…' : 'Войти в комнату'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
