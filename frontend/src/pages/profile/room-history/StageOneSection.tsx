import { useState } from 'react'
import type {
  RoomHistoryStageOneSectionDTO,
  RoomHistoryStageOneVoteDTO,
} from '../../../api/types'
import { StageOneRestaurantItem } from './StageOneRestaurantItem'
import '../../../styles/ProfilePage.css'

type StageOneSectionProps = {
  section: RoomHistoryStageOneSectionDTO | null
  loading: boolean
  error: string | null
  stageOneVotes: Record<number, RoomHistoryStageOneVoteDTO[]>
  stageOneVotesLoading: Record<number, boolean>
  stageOneVotesError: Record<number, string | null>
  onLoadVotes: (restaurantId: number) => void | Promise<void>
}

export function StageOneSection({
  section,
  loading,
  error,
  stageOneVotes,
  stageOneVotesLoading,
  stageOneVotesError,
  onLoadVotes,
}: StageOneSectionProps) {
  const [open, setOpen] = useState(false)
  const [restaurantListOpen, setRestaurantListOpen] = useState(false)
  const n = section?.restaurants.length ?? 0
  const hasRelaxedQuorumIncluded =
    section?.restaurants.some((r) => r.includedBy === 'RELAXED_QUORUM') ?? false
  const hasOrganizerDoubleIncluded =
    section?.restaurants.some((r) => r.includedBy === 'ORGANIZER_DOUBLE') ?? false

  return (
    <section className="profile-room__section">
      <button
        type="button"
        className="profile-room__info-toggle"
        aria-expanded={open}
        onClick={() => setOpen((v) => !v)}
      >
        <span className="profile-room__section-title">Этап 1 — голосование</span>
        <span className="profile-room__rest-meta">{open ? 'Свернуть' : 'Развернуть'}</span>
      </button>
      {loading && <p className="profile-room__notice">Загружаем этап 1…</p>}
      {error && !loading && <p className="profile__error">{error}</p>}
      {!loading && !error && section && open && (
        <div className="profile-room__info-content">
          <div className="profile-room__grid">
            <div>
              <div className="profile-room__field-label">Порог прохода</div>
              <div className="profile-room__field-value">{section.outcome.baseQuorum}</div>
            </div>
            {hasRelaxedQuorumIncluded && (
              <div>
                <div className="profile-room__field-label">Порог прохода (резервный)</div>
                <div className="profile-room__field-value">{section.outcome.relaxedQuorum}</div>
              </div>
            )}
            {hasOrganizerDoubleIncluded && (
              <div>
                <div className="profile-room__field-label">Удвоение голоса организатора</div>
                <div className="profile-room__field-value">Применялось</div>
              </div>
            )}
          </div>

          <div className="profile-room__stage-one-restaurants">
            <div className="profile-room__field-label">Просмотренные рестораны</div>
            {n === 0 ? (
              <p className="profile-room__notice">Нет данных по этапу 1.</p>
            ) : (
              <>
                <button
                  type="button"
                  className="profile-room__stage-one-list-toggle"
                  aria-expanded={restaurantListOpen}
                  onClick={() => setRestaurantListOpen((v) => !v)}
                >
                  {restaurantListOpen
                    ? 'Свернуть список'
                    : `Показать список (${n})`}
                </button>
                <div className="profile-room__stage-one-list-wrap" hidden={!restaurantListOpen}>
                  <ul className="profile-room__stage-one-list">
                    {section.restaurants.map((row) => (
                      <StageOneRestaurantItem
                        key={row.restaurantId}
                        row={row}
                        votes={stageOneVotes[row.restaurantId]}
                        votesLoading={Boolean(stageOneVotesLoading[row.restaurantId])}
                        votesError={stageOneVotesError[row.restaurantId] ?? null}
                        onLoadVotes={() => onLoadVotes(row.restaurantId)}
                      />
                    ))}
                  </ul>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </section>
  )
}
