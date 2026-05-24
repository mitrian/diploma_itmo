import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import '../../styles/AuthPages.css'

export type RegisterPageViewProps = {
  loginName: string
  displayName: string
  password: string
  error: string | null
  pending: boolean
  onLoginNameChange: (value: string) => void
  onDisplayNameChange: (value: string) => void
  onPasswordChange: (value: string) => void
  onSubmit: (e: FormEvent) => void
}

export function RegisterPageView({
  loginName,
  displayName,
  password,
  error,
  pending,
  onLoginNameChange,
  onDisplayNameChange,
  onPasswordChange,
  onSubmit,
}: RegisterPageViewProps) {
  return (
    <div className="auth-shell">
      <div className="auth-panel">
        <div className="auth-page">
          <h1 className="auth-page__title">Регистрация</h1>
          <p className="auth-page__hint">Пароль не короче 12 символов</p>
          <form className="auth-form" onSubmit={onSubmit}>
            <label className="auth-form__field">
              <span>Логин</span>
              <input
                type="text"
                autoComplete="username"
                value={loginName}
                onChange={(e) => onLoginNameChange(e.target.value)}
                required
                minLength={3}
              />
            </label>
            <label className="auth-form__field">
              <span>Отображаемое имя</span>
              <input
                type="text"
                value={displayName}
                onChange={(e) => onDisplayNameChange(e.target.value)}
                required
              />
            </label>
            <label className="auth-form__field">
              <span>Пароль</span>
              <input
                type="password"
                autoComplete="new-password"
                value={password}
                onChange={(e) => onPasswordChange(e.target.value)}
                required
                minLength={12}
              />
            </label>
            {error && <p className="auth-form__error">{error}</p>}
            <button type="submit" className="auth-form__submit" disabled={pending}>
              {pending ? 'Создание…' : 'Зарегистрироваться'}
            </button>
          </form>
          <p className="auth-page__footer">
            Уже есть аккаунт? <Link to="/login">Войти</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
