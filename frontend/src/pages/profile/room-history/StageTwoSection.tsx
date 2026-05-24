import { useState } from 'react'
import type { RoomHistoryStageTwoRowDTO, WinnerSelectionPrinciple } from '../../../api/types'
import { stageTwoEmptyExplanation } from './labels'
import { StageTwoTable } from './StageTwoTable'
import '../../../styles/ProfilePage.css'

type StageTwoSectionProps = {
  rows: RoomHistoryStageTwoRowDTO[] | null
  loading: boolean
  error: string | null
  winnerId: number | null
  winnerPrinciple: WinnerSelectionPrinciple
}

export function StageTwoSection({
  rows,
  loading,
  error,
  winnerId,
  winnerPrinciple,
}: StageTwoSectionProps) {
  const [open, setOpen] = useState(false)

  return (
    <section className="profile-room__section">
      <button
        type="button"
        className="profile-room__info-toggle"
        aria-expanded={open}
        onClick={() => setOpen((v) => !v)}
      >
        <span className="profile-room__section-title">Этап 2 — ранжирование</span>
        <span className="profile-room__rest-meta">{open ? 'Свернуть' : 'Развернуть'}</span>
      </button>
      {loading && <p className="profile-room__notice">Загружаем этап 2…</p>}
      {error && !loading && <p className="profile__error">{error}</p>}
      {!loading && !error && open && (
        <div className="profile-room__info-content">
          {rows && rows.length === 0 && (
            <p className="profile-room__notice">
              {stageTwoEmptyExplanation(winnerPrinciple, winnerId)}
            </p>
          )}
          {rows && rows.length > 0 && <StageTwoTable rows={rows} winnerId={winnerId} />}
        </div>
      )}
    </section>
  )
}
