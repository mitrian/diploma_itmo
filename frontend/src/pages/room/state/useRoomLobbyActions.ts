import { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { patchRoomReady, postLeaveRoom, roomPasswordStorageKey } from '../../../api/roomsApi'
import type { RoomDetailsResponseDTO } from '../../../api/types'

type ApplyDetails = (d: RoomDetailsResponseDTO) => void

export function useRoomLobbyActions(code: string, applyServerDetails: ApplyDetails) {
  const navigate = useNavigate()
  const [readySaving, setReadySaving] = useState(false)
  const [readyError, setReadyError] = useState<string | null>(null)
  const [leaveSaving, setLeaveSaving] = useState(false)
  const [leaveError, setLeaveError] = useState<string | null>(null)

  const onSetReady = useCallback(
    async (ready: boolean) => {
      if (!code) return
      setReadyError(null)
      setReadySaving(true)
      try {
        const d = await patchRoomReady(code, ready)
        applyServerDetails(d)
      } catch (e) {
        setReadyError(e instanceof Error ? e.message : 'Не удалось изменить готовность')
      } finally {
        setReadySaving(false)
      }
    },
    [code, applyServerDetails],
  )

  const onLeaveRoom = useCallback(async () => {
    if (!code) return
    setLeaveError(null)
    setLeaveSaving(true)
    try {
      await postLeaveRoom(code)
      sessionStorage.removeItem(roomPasswordStorageKey(code))
      navigate('/room', { replace: true })
    } catch (e) {
      setLeaveError(e instanceof Error ? e.message : 'Не удалось выйти из комнаты')
    } finally {
      setLeaveSaving(false)
    }
  }, [code, navigate])

  return {
    readySaving,
    readyError,
    onSetReady,
    leaveSaving,
    leaveError,
    onLeaveRoom,
  }
}
