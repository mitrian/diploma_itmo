import { useState } from 'react'
import type {
  RoomHistoryFiltersDTO,
  RoomHistoryOverviewDTO,
  RoomHistoryParticipantDTO,
} from '../../../api/types'
import { formatDateTime, formatVotingDuration } from './labels'
import '../../../styles/ProfilePage.css'

type RoomInfoSectionProps = {
  overview: RoomHistoryOverviewDTO | null
  participants: RoomHistoryParticipantDTO[] | null
  filters: RoomHistoryFiltersDTO | null
  loading: {
    overview: boolean
    participants: boolean
    filters: boolean
  }
  error: string | null
}

export function RoomInfoSection({
  overview,
  participants,
  filters,
  loading,
  error,
}: RoomInfoSectionProps) {
  const [open, setOpen] = useState(false)
  const anyLoading = loading.overview || loading.participants || loading.filters

  return (
    <section className="profile-room__section">
      <button
        type="button"
        className="profile-room__info-toggle"
        aria-expanded={open}
        onClick={() => setOpen((v) => !v)}
      >
        <span className="profile-room__section-title">Информация о комнате</span>
        <span className="profile-room__rest-meta">{open ? 'Свернуть' : 'Развернуть'}</span>
      </button>
      {anyLoading && <p className="profile-room__notice">Загружаем информацию о комнате…</p>}
      {error && !anyLoading && <p className="profile__error">{error}</p>}
      {!anyLoading && !error && (!overview || !filters || !participants) && (
        <p className="profile-room__notice">Не удалось загрузить данные комнаты.</p>
      )}
      {!anyLoading && !error && overview && filters && participants && open && (
        <div className="profile-room__info-content">
          <div className="profile-room__grid">
            <div>
              <div className="profile-room__field-label">Код комнаты</div>
              <div className="profile-room__field-value">{overview.roomCode}</div>
            </div>
            <div>
              <div className="profile-room__field-label">Создана</div>
              <div className="profile-room__field-value">{formatDateTime(overview.createdAt)}</div>
            </div>
            <div>
              <div className="profile-room__field-label">Завершена</div>
              <div className="profile-room__field-value">{formatDateTime(overview.finishedAt)}</div>
            </div>
            <div>
              <div className="profile-room__field-label">Голосование</div>
              <div className="profile-room__field-value">
                {formatVotingDuration(overview.votingDurationSeconds)}
              </div>
            </div>
            <div>
              <div className="profile-room__field-label">Организатор</div>
              <div className="profile-room__field-value">{overview.ownerDisplayName}</div>
            </div>
            <div>
              <div className="profile-room__field-label">Участников</div>
              <div className="profile-room__field-value">{overview.participantCount}</div>
            </div>
            <div>
              <div className="profile-room__field-label">Центр (lat, lon)</div>
              <div className="profile-room__field-value">
                {filters.centerLat !== null && filters.centerLon !== null
                  ? `${filters.centerLat.toFixed(5)}, ${filters.centerLon.toFixed(5)}`
                  : '—'}
              </div>
            </div>
            <div>
              <div className="profile-room__field-label">Радиус</div>
              <div className="profile-room__field-value">
                {filters.maxDistanceMeters ? `${filters.maxDistanceMeters} м` : '—'}
              </div>
            </div>
          </div>
          <div className="profile-room__info-block">
            <div className="profile-room__field-label">Кухонные теги</div>
            {filters.kitchenTags.length === 0 ? (
              <p className="profile-room__notice">Не задавались.</p>
            ) : (
              <ul className="profile-room__chips" style={{ marginTop: '0.4rem' }}>
                {filters.kitchenTags.map((t) => (
                  <li key={`${t.slug}-${t.pickedByLogin}`} className="profile-room__chip">
                    {t.labelRu}
                    <span className="profile-room__rest-meta" style={{ marginLeft: '0.4rem' }}>
                      {t.pickedByDisplayName}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
          <div className="profile-room__info-block">
            <div className="profile-room__field-label">Участники</div>
            <ul className="profile-room__participants">
              {participants.map((p, idx) => (
                <li key={`${p.displayName}-${idx}`} className="profile-room__participant">
                  <span>{p.displayName}</span>
                  {p.owner && <span className="profile-room__participant-owner">Владелец</span>}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </section>
  )
}
