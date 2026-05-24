export type LobbyReadyPanelProps = {
  currentUserReady: boolean
  readySaving: boolean
  leaveSaving: boolean
  readyError: string | null
  leaveError: string | null
  onSetReady: (ready: boolean) => void | Promise<void>
  onLeaveRoom: () => void | Promise<void>
}

export function LobbyReadyPanel({
  currentUserReady,
  readySaving,
  leaveSaving,
  readyError,
  leaveError,
  onSetReady,
  onLeaveRoom,
}: LobbyReadyPanelProps) {
  return (
    <div className="room-detail__panel">
      <h2 className="room-detail__section-title">Ваша готовность</h2>
      <div className="room-detail__ready-actions">
        {currentUserReady ? (
          <button
            type="button"
            className="room-detail__btn room-detail__btn--secondary"
            disabled={readySaving || leaveSaving}
            onClick={() => void onSetReady(false)}
          >
            {readySaving ? 'Сохранение…' : 'Я не готов'}
          </button>
        ) : (
          <button
            type="button"
            className="room-detail__btn room-detail__btn--primary"
            disabled={readySaving || leaveSaving}
            onClick={() => void onSetReady(true)}
          >
            {readySaving ? 'Сохранение…' : 'Я готов'}
          </button>
        )}
        <button
          type="button"
          className="room-detail__btn room-detail__btn--leave"
          disabled={readySaving || leaveSaving}
          onClick={() => void onLeaveRoom()}
        >
          {leaveSaving ? 'Выход…' : 'Выйти из комнаты'}
        </button>
      </div>
      {readyError && <p className="room-detail__ready-error">{readyError}</p>}
      {leaveError && <p className="room-detail__ready-error">{leaveError}</p>}
    </div>
  )
}
