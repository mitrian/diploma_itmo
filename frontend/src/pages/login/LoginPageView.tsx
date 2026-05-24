import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import '../../styles/AuthPages.css'

export type LoginPageViewProps = {
  loginName: string
  password: string
  error: string | null
  pending: boolean
  onLoginNameChange: (value: string) => void
  onPasswordChange: (value: string) => void
  onSubmit: (e: FormEvent) => void
}

export function LoginPageView({
  loginName,
  password,
  error,
  pending,
  onLoginNameChange,
  onPasswordChange,
  onSubmit,
}: LoginPageViewProps) {
  return (
    <div className="auth-shell">
      <div className="auth-panel">
        <div className="auth-page">
          <h1 className="auth-page__title">Вход</h1>
          <p className="auth-page__lead">
            Войдите, чтобы создавать комнаты и выбирать ресторан вместе с компанией.
          </p>
          <form className="auth-form" onSubmit={onSubmit}>
            <label className="auth-form__field">
              <span>Логин</span>
              <input
                type="text"
                autoComplete="username"
                value={loginName}
                onChange={(e) => onLoginNameChange(e.target.value)}
                required
              />
            </label>
            <label className="auth-form__field">
              <span>Пароль</span>
              <input
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => onPasswordChange(e.target.value)}
                required
              />
            </label>
            {error && <p className="auth-form__error">{error}</p>}
            <button type="submit" className="auth-form__submit" disabled={pending}>
              {pending ? 'Вход…' : 'Войти'}
            </button>
          </form>
          <p className="auth-page__footer">
            Нет аккаунта? <Link to="/register">Регистрация</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
