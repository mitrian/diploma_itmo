import type { RoomHistoryOverviewDTO, RoomWinnerResponseDTO } from '../../../api/types'
import { principleLabel } from './labels'
import '../../../styles/ProfilePage.css'

type WinnerSectionProps = {
  overview: RoomHistoryOverviewDTO | null
  winner: RoomWinnerResponseDTO | null
  loading: boolean
  error: string | null
}

export function WinnerSection({ overview, winner, loading, error }: WinnerSectionProps) {
  return (
    <section className="profile-room__section">
      <h2 className="profile-room__section-title">Победитель</h2>
      {loading && <p className="profile-room__notice">Загружаем карточку победителя…</p>}
      {error && !loading && <p className="profile__error">{error}</p>}
      {!loading && !error && overview && (
        <>
          <h3 className="profile-room__winner-name">
            {winner?.winnerRestaurant?.name ?? overview.winnerRestaurantName ?? 'Не выбран'}
          </h3>
          <p className="profile-room__principle">{principleLabel(overview.winnerPrinciple)}</p>
          {winner?.winnerRestaurant && (
            <div className="profile-room__grid" style={{ marginTop: '0.75rem' }}>
              <div>
                <div className="profile-room__field-label">Адрес</div>
                <div className="profile-room__field-value">{winner.winnerRestaurant.address}</div>
              </div>
              <div>
                <div className="profile-room__field-label">Время работы</div>
                <div className="profile-room__field-value">
                  {winner.winnerRestaurant.openingHours}
                </div>
              </div>
              <div>
                <div className="profile-room__field-label">Телефон</div>
                <div className="profile-room__field-value">{winner.winnerRestaurant.phone}</div>
              </div>
              {winner.winnerRestaurant.websiteUrl && (
                <div>
                  <div className="profile-room__field-label">Сайт</div>
                  <div className="profile-room__field-value">
                    <a
                      href={winner.winnerRestaurant.websiteUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {winner.winnerRestaurant.websiteUrl}
                    </a>
                  </div>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </section>
  )
}
