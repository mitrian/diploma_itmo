import { useCallback, useEffect, useState, type MutableRefObject } from 'react'
import { postStartSession } from '../../../api/roomsApi'
import type { RoomDetailsResponseDTO } from '../../../api/types'

type ApplyDetails = (d: RoomDetailsResponseDTO) => void

export function useRoomSessionStart(
  code: string,
  detailsState: RoomDetailsResponseDTO['state'] | undefined,
  detailsRef: MutableRefObject<RoomDetailsResponseDTO | null>,
  applyServerDetails: ApplyDetails,
) {
  const [startSaving, setStartSaving] = useState(false)
  const [startError, setStartError] = useState<string | null>(null)
  const [startInfo, setStartInfo] = useState<string | null>(null)

  useEffect(() => {
    if (detailsState !== 'GEO_FILTERS') {
      setStartInfo(null)
    }
  }, [detailsState])

  const onStartSession = useCallback(async () => {
    if (!code) return
    setStartError(null)
    setStartInfo(null)
    setStartSaving(true)
    const prevState = detailsRef.current?.state
    try {
      const d = await postStartSession(code)
      applyServerDetails(d)
      if (prevState === 'AWAITING_START' && d.state === 'GEO_FILTERS') {
        setStartInfo(
          'По выбранным фильтрам не найдено ни одного ресторана. Геозона и кухонные теги сброшены — задайте их заново.',
        )
      }
    } catch (e) {
      setStartError(e instanceof Error ? e.message : 'Не удалось начать сессию')
    } finally {
      setStartSaving(false)
    }
  }, [code, applyServerDetails, detailsRef])

  return {
    startSaving,
    startError,
    startInfo,
    onStartSession,
  }
}
