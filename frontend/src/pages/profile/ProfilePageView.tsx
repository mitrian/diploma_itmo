import { Link } from 'react-router-dom'
import type { RoomHistorySummaryDTO } from '../../api/types'
import { formatVotingDuration } from './room-history/labels'
import '../../styles/ProfilePage.css'

type ProfilePageViewProps = {
  items: RoomHistorySummaryDTO[] | null
  loading: boolean
  error: string | null
  notice: string | null
}

function formatDateTime(iso: string): string {
  try {
    const date = new Date(iso)
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return iso
  }
}

export function ProfilePageView({ items, loading, error, notice }: ProfilePageViewProps) {
  return (
    <div className="profile">
      <div className="profile__header">
        <h1 className="profile__title">Личный кабинет</h1>
      </div>

      {notice && <p className="profile__notice">{notice}</p>}

      <section className="profile__section">
        <h2 className="profile__section-title">История сессий</h2>

        {loading && <p className="profile__muted">Загружаем историю…</p>}
        {error && !loading && <p className="profile__error">{error}</p>}
        {!loading && !error && items && items.length === 0 && (
          <p className="profile__muted">Пока нет завершённых сессий.</p>
        )}

        {!loading && !error && items && items.length > 0 && (
          <ul className="profile-history">
            {items.map((it) => (
              <li key={it.roomCode} className="profile-history__item">
                <Link to={`/profile/rooms/${it.roomCode}`} className="profile-history__link">
                  <div className="profile-history__row">
                    <span className="profile-history__code">{it.roomCode}</span>
                    {it.viewerWasOwner && (
                      <span className="profile-history__badge">Владелец</span>
                    )}
                  </div>
                  <div className="profile-history__row profile-history__row--meta">
                    <span className="profile-history__meta">
                      {formatDateTime(it.finishedAt)}
                    </span>
                    <span className="profile-history__meta">
                      Участников: {it.participantCount}
                    </span>
                    {it.votingDurationSeconds != null && (
                      <span className="profile-history__meta">
                        Голосование: {formatVotingDuration(it.votingDurationSeconds)}
                      </span>
                    )}
                  </div>
                  <div className="profile-history__row">
                    <span className="profile-history__winner-label">Победитель:</span>
                    <span className="profile-history__winner-name">
                      {it.winnerRestaurantName ?? 'Не выбран'}
                    </span>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
