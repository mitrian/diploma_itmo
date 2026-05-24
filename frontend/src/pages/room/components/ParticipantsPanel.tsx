import type { RoomParticipantViewDTO } from '../../../api/types'

export function ParticipantsPanel({
  participants,
  showKitchenFilterStatus = false,
}: {
  participants: RoomParticipantViewDTO[]
  showKitchenFilterStatus?: boolean
}) {
  return (
    <div className="room-detail__panel">
      <h2 className="room-detail__section-title">Участники ({participants.length})</h2>
      <ul className="room-detail__list">
        {participants.map((p, i) => (
          <li key={`${p.displayName}-${i}`} className="room-detail__participant">
            <div>
              <div className="room-detail__name">{p.displayName}</div>
            </div>
            <div className="room-detail__badges">
              {p.owner && <span className="room-detail__badge">Владелец</span>}
              {p.ready ? (
                <span className="room-detail__badge room-detail__badge--ready">Готов</span>
              ) : (
                <span className="room-detail__badge room-detail__badge--not-ready">Не готов</span>
              )}
              {showKitchenFilterStatus &&
                (p.filtersConfirmed === true ? (
                  <span className="room-detail__badge room-detail__badge--filters-ok">Фильтры ✓</span>
                ) : (
                  <span className="room-detail__badge room-detail__badge--filters-pending">Фильтры</span>
                ))}
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
