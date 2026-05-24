import { RoomPageView } from './RoomPageView'
import { useRoomPageState } from './state/useRoomPageState'

export function RoomPage() {
  const state = useRoomPageState()
  return <RoomPageView {...state} />
}
