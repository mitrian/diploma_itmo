import { useState } from 'react'
import type { RoomHistoryStageOneRowDTO, RoomHistoryStageOneVoteDTO } from '../../../api/types'
import { inclusionLabel } from './labels'
import '../../../styles/ProfilePage.css'

type StageOneRestaurantItemProps = {
  row: RoomHistoryStageOneRowDTO
  votes: RoomHistoryStageOneVoteDTO[] | undefined
  votesLoading: boolean
  votesError: string | null
  onLoadVotes: () => void | Promise<void>
}

export function StageOneRestaurantItem({
  row,
  votes,
  votesLoading,
  votesError,
  onLoadVotes,
}: StageOneRestaurantItemProps) {
  const [open, setOpen] = useState(false)
  const inclusion = inclusionLabel(row.includedBy)

  function toggle() {
    setOpen((prev) => {
      const next = !prev
      if (next && !votes && !votesLoading) {
        void onLoadVotes()
      }
      return next
    })
  }

  return (
    <li className="profile-room__stage-one-item">
      <article className="profile-room__rest-card">
        <header className="profile-room__rest-card-header">
          <h3 className="profile-room__rest-name">{row.name}</h3>
          {inclusion && <span className="profile-room__included-by">{inclusion}</span>}
        </header>
        <p className="profile-room__rest-line">
          <span className="profile-room__rest-key">Адрес</span>
          {row.address}
        </p>
        <p className="profile-room__rest-line">
          <span className="profile-room__rest-key">Время работы</span>
          {row.openingHours}
        </p>
        <p className="profile-room__rest-line">
          <span className="profile-room__rest-key">Телефон</span>
          <a href={`tel:${row.phone.replace(/\s/g, '')}`} className="profile-room__rest-link">
            {row.phone}
          </a>
        </p>
        <p className="profile-room__rest-line">
          <span className="profile-room__rest-key">Сайт</span>
          {row.websiteUrl ? (
            <a
              href={row.websiteUrl}
              className="profile-room__rest-link"
              target="_blank"
              rel="noopener noreferrer"
            >
              {row.websiteUrl}
            </a>
          ) : (
            <span className="profile-room__rest-meta">не указан</span>
          )}
        </p>
        {row.kitchenTags.length > 0 && (
          <p className="profile-room__rest-line">
            <span className="profile-room__rest-key">Кухни</span>
            <span className="profile-room__rest-tags">
              {row.kitchenTags.map((t) => (
                <span key={t.id} className="profile-room__chip">
                  {t.labelRu}
                </span>
              ))}
            </span>
          </p>
        )}
        <div className="profile-room__rest-counters">
          <span className="profile-room__counter">
            <span className="profile-room__counter-label">Проголосовали «подходит»:</span>
            <span className="profile-room__counter-value profile-room__counter-value--pos">
              {row.totalSuitable}
            </span>
          </span>
          <span className="profile-room__counter">
            <span className="profile-room__counter-label">Проголосовали «не подходит»:</span>
            <span className="profile-room__counter-value profile-room__counter-value--neg">
              {row.totalUnsuitable}
            </span>
          </span>
          <button type="button" className="profile-room__votes-toggle" onClick={toggle}>
            {open ? 'Скрыть голоса' : 'Показать голоса'}
          </button>
        </div>
      </article>
      {open && (
        <div className="profile-room__stage-one-details">
          {votesLoading && <p className="profile-room__notice">Загружаем голоса…</p>}
          {votesError && !votesLoading && <p className="profile__error">{votesError}</p>}
          {votes && votes.length > 0 && (
            <ul className="profile-room__votes-list">
              {votes.map((v, idx) => (
                <li
                  key={`${v.userDisplayName}-${idx}`}
                  className="profile-room__vote-row"
                >
                  <span>{v.userDisplayName}</span>
                  <span
                    className={`profile-room__vote-suitable profile-room__vote-suitable--${v.suitable ? 'yes' : 'no'}`}
                  >
                    {v.suitable ? 'Подходит' : 'Не подходит'}
                  </span>
                </li>
              ))}
            </ul>
          )}
          {votes && votes.length === 0 && !votesLoading && (
            <p className="profile-room__notice">Голосов не было.</p>
          )}
        </div>
      )}
    </li>
  )
}
