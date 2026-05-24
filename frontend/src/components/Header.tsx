import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import '../styles/Header.css'

export function Header() {
  const { user, loading, logout } = useAuth()

  return (
    <header className="app-header">
      <div className="app-header__inner">
        <div className="app-header__left">
          <Link to="/" className="app-header__brand">
            Diploma
          </Link>
          {!loading && user && (
            <nav className="app-header__nav" aria-label="Разделы">
              <Link to="/room" className="app-header__nav-link">
                Комната
              </Link>
            </nav>
          )}
        </div>
        <div className="app-header__actions">
          {loading ? (
            <span className="app-header__muted">…</span>
          ) : user ? (
            <>
              <Link
                to="/profile"
                className="app-header__user app-header__user--link"
                title={`${user.login} — личный кабинет`}
              >
                {user.displayName}
              </Link>
              <button type="button" className="app-header__btn-secondary" onClick={logout}>
                Выйти
              </button>
            </>
          ) : (
            <Link to="/login" className="app-header__btn-primary">
              Войти
            </Link>
          )}
        </div>
      </div>
    </header>
  )
}
