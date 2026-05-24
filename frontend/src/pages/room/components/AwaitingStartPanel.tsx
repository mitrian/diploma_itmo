export type AwaitingStartPanelProps = {
  currentUserIsOwner: boolean
  startSaving: boolean
  startError: string | null
  onStartSession: () => void | Promise<void>
}

export function AwaitingStartPanel({
  currentUserIsOwner,
  startSaving,
  startError,
  onStartSession,
}: AwaitingStartPanelProps) {
  return (
    <div className="room-detail__panel">
      <h2 className="room-detail__section-title">Запуск сессии</h2>
      {currentUserIsOwner ? (
        <>
          <p className="room-detail__hint">Фильтры настроены. Начните сессию коллективного выбора.</p>
          <div className="room-detail__ready-actions">
            <button
              type="button"
              className="room-detail__btn room-detail__btn--primary"
              disabled={startSaving}
              onClick={() => void onStartSession()}
            >
              {startSaving ? 'Запуск…' : 'Начать сессию'}
            </button>
          </div>
          {startError && <p className="room-detail__ready-error">{startError}</p>}
        </>
      ) : (
        <p className="room-detail__hint">Ожидаем, когда владелец комнаты начнёт сессию.</p>
      )}
    </div>
  )
}
