import type { RoomPageViewProps } from '../RoomPageView.types'
import { useRoomDetailsCore } from './useRoomDetailsCore'
import { useRoomGeoFilter } from './useRoomGeoFilter'
import { useRoomKitchenFilters } from './useRoomKitchenFilters'
import { useRoomLobbyActions } from './useRoomLobbyActions'
import { useRoomSessionStart } from './useRoomSessionStart'

export function useRoomPageState(): RoomPageViewProps {
  const {
    code,
    details,
    loading,
    error,
    applyServerDetails,
    refreshRoomDetails,
    detailsRef,
    ownerPlainPassword,
    currentUserReady,
  } = useRoomDetailsCore()

  const lobbyActions = useRoomLobbyActions(code, applyServerDetails)
  const geo = useRoomGeoFilter(code, details, applyServerDetails)
  const sessionStart = useRoomSessionStart(code, details?.state, detailsRef, applyServerDetails)
  const kitchen = useRoomKitchenFilters(code, details, applyServerDetails)

  return {
    summary: {
      loading,
      error,
      details,
      refreshRoomDetails,
      ownerPlainPassword,
    },
    lobby: {
      currentUserReady,
      readySaving: lobbyActions.readySaving,
      readyError: lobbyActions.readyError,
      onSetReady: lobbyActions.onSetReady,
      leaveSaving: lobbyActions.leaveSaving,
      leaveError: lobbyActions.leaveError,
      onLeaveRoom: lobbyActions.onLeaveRoom,
    },
    geo,
    sessionStart,
    kitchen,
  }
}
