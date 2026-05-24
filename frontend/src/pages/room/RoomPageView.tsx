import { Link } from 'react-router-dom'
import type { RoomState } from '../../api/types'
import { AwaitingStartPanel } from './components/AwaitingStartPanel'
import { GeoFiltersPanel } from './components/GeoFiltersPanel'
import { KitchenFiltersPanel } from './components/KitchenFiltersPanel'
import { LobbyReadyPanel } from './components/LobbyReadyPanel'
import { ParticipantsPanel } from './components/ParticipantsPanel'
import { StageOneFinishedWinner } from './components/StageOneFinishedWinner'
import { StageOneSwipeDeck } from './components/StageOneSwipeDeck'
import { StageTwoRankingPanel } from './components/StageTwoRankingPanel'
import type { RoomPageViewProps } from './RoomPageView.types'
import '../../styles/RoomPage.css'

export type { RoomPageViewProps } from './RoomPageView.types'

function roomStateLabel(state: RoomState): string {
  switch (state) {
    case 'LOBBY':
      return 'Лобби'
    case 'GEO_FILTERS':
      return 'Геофильтры'
    case 'AWAITING_START':
      return 'Ожидание запуска'
    case 'STAGE_ONE':
      return 'Первый этап'
    case 'STAGE_TWO':
      return 'Второй этап'
    case 'FINISHED':
      return 'Сессия завершена'
    default:
      return state
  }
}

export function RoomPageView({ summary, lobby, geo, sessionStart, kitchen }: RoomPageViewProps) {
  const { loading, error, details, refreshRoomDetails, ownerPlainPassword } = summary

  if (loading) {
    return (
      <div className="room-detail">
        <p className="room-detail__loading">Загрузка…</p>
      </div>
    )
  }

  if (error || !details) {
    return (
      <div className="room-detail">
        <Link to="/room" className="room-detail__back">
          ← К комнате
        </Link>
        <p className="room-detail__error">{error ?? 'Комната недоступна'}</p>
      </div>
    )
  }

  const canNavigateAwayViaRoomEntry = details.state === 'LOBBY'
  const showRoomSummaryAndParticipants =
    details.state !== 'STAGE_ONE' && details.state !== 'STAGE_TWO' && details.state !== 'FINISHED'

  const stageTwoWide = details.state === 'STAGE_TWO'

  return (
    <div
      className={`room-detail${showRoomSummaryAndParticipants ? '' : ' room-detail--voting-focus'}${stageTwoWide ? ' room-detail--stage-two-wide' : ''}`}
    >
      {canNavigateAwayViaRoomEntry && (
        <Link to="/room" className="room-detail__back">
          ← Создать или войти в другую комнату
        </Link>
      )}
      {showRoomSummaryAndParticipants && (
        <>
          <h1 className="room-detail__title">Комната</h1>

          <div className="room-detail__panel">
            <div className="room-detail__row">
              <span className="room-detail__label">Код комнаты</span>
              <span className="room-detail__value">{details.code}</span>
            </div>
            <div className="room-detail__row">
              <span className="room-detail__label">Состояние</span>
              <span className="room-detail__value room-detail__value--state">{roomStateLabel(details.state)}</span>
            </div>
            <div className="room-detail__row">
              <span className="room-detail__label">Центр</span>
              <span className="room-detail__value room-detail__value--normal">
                {details.centerLat == null || details.centerLon == null
                  ? 'Не задан'
                  : `${details.centerLat}, ${details.centerLon}`}
              </span>
            </div>
            <div className="room-detail__row">
              <span className="room-detail__label">Радиус (м)</span>
              <span className="room-detail__value room-detail__value--normal">
                {details.maxDistanceMeters == null ? 'Не задан' : details.maxDistanceMeters}
              </span>
            </div>
            {details.currentUserIsOwner && ownerPlainPassword && (
              <div className="room-detail__row">
                <span className="room-detail__label">Пароль комнаты</span>
                <span className="room-detail__value">{ownerPlainPassword}</span>
              </div>
            )}
            {details.currentUserIsOwner && !ownerPlainPassword && (
              <p className="room-detail__hint">
                Пароль не хранится на сервере. Если вы обновили страницу и не сохранили пароль отдельно,
                задайте новую комнату или используйте тот пароль, который вы указывали при создании.
              </p>
            )}
          </div>
        </>
      )}

      {details.state === 'LOBBY' && lobby.currentUserReady !== null && (
        <LobbyReadyPanel
          currentUserReady={lobby.currentUserReady}
          readySaving={lobby.readySaving}
          leaveSaving={lobby.leaveSaving}
          readyError={lobby.readyError}
          leaveError={lobby.leaveError}
          onSetReady={lobby.onSetReady}
          onLeaveRoom={lobby.onLeaveRoom}
        />
      )}

      {details.state === 'GEO_FILTERS' && sessionStart.startInfo && (
        <p className="room-detail__hint room-detail__hint--notice">{sessionStart.startInfo}</p>
      )}

      {details.state === 'GEO_FILTERS' && (
        <GeoFiltersPanel currentUserIsOwner={details.currentUserIsOwner} {...geo} />
      )}

      {details.state === 'AWAITING_START' && (
        <>
          <KitchenFiltersPanel
            catalog={kitchen.kitchenCatalog}
            catalogLoading={kitchen.kitchenCatalogLoading}
            catalogError={kitchen.kitchenCatalogError}
            roomKitchenTags={details.roomKitchenTags ?? []}
            myKitchenTagSlugs={details.myKitchenTagSlugs ?? []}
            kitchenFiltersLocked={kitchen.kitchenFiltersLocked}
            currentUserFiltersConfirmed={kitchen.currentUserFiltersConfirmed}
            pendingSlugs={kitchen.pendingKitchenSlugs}
            onPendingSlugsChange={kitchen.setPendingKitchenSlugs}
            onAddSelected={kitchen.onAddKitchenTags}
            onRemoveSlug={kitchen.onRemoveKitchenTag}
            onConfirmFilters={kitchen.onConfirmKitchenFilters}
            addSaving={kitchen.kitchenAddSaving}
            removeLoadingSlug={kitchen.kitchenRemoveSlug}
            confirmSaving={kitchen.kitchenConfirmSaving}
            actionError={kitchen.kitchenActionError}
            getTagLabel={kitchen.getKitchenTagLabel}
          />
          <AwaitingStartPanel
            currentUserIsOwner={details.currentUserIsOwner}
            startSaving={sessionStart.startSaving}
            startError={sessionStart.startError}
            onStartSession={sessionStart.onStartSession}
          />
        </>
      )}

      {details.state === 'STAGE_ONE' && (
        <div className="room-detail__panel room-detail__panel--stage-one">
          <StageOneSwipeDeck roomCode={details.code} refreshRoomDetails={refreshRoomDetails} />
        </div>
      )}

      {details.state === 'STAGE_TWO' && (
        <div className="room-detail__panel room-detail__panel--stage-one">
          <div className="room-detail__state-banner">
            <span className="room-detail__label">Состояние</span>
            <span className="room-detail__value room-detail__value--state">{roomStateLabel(details.state)}</span>
          </div>
          <StageTwoRankingPanel roomCode={details.code} refreshRoomDetails={refreshRoomDetails} />
        </div>
      )}

      {details.state === 'FINISHED' && (
        <div className="room-detail__panel room-detail__panel--stage-one">
          <StageOneFinishedWinner roomCode={details.code} />
        </div>
      )}

      {showRoomSummaryAndParticipants && (
        <ParticipantsPanel
          participants={details.participants}
          showKitchenFilterStatus={details.state === 'AWAITING_START'}
        />
      )}
    </div>
  )
}
