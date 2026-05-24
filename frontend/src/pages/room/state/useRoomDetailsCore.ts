import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getRoomByCode, getRoomByCodeRequestHttpStatus, roomPasswordStorageKey } from '../../../api/roomsApi'
import type { RoomDetailsResponseDTO } from '../../../api/types'
import { ROOM_POLL_INTERVAL_MS } from '../../../config/polling'
import { useAuth } from '../../../context/AuthContext'

type LocationState = {
  roomPassword?: string
}

export function useRoomDetailsCore() {
  const { roomCode: roomCodeParam } = useParams<{ roomCode: string }>()
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()

  const [details, setDetails] = useState<RoomDetailsResponseDTO | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const detailsRevisionRef = useRef(0)
  const detailsRef = useRef<RoomDetailsResponseDTO | null>(null)

  const code = useMemo(() => {
    if (!roomCodeParam) return ''
    try {
      return decodeURIComponent(roomCodeParam).trim().toUpperCase()
    } catch {
      return roomCodeParam.trim().toUpperCase()
    }
  }, [roomCodeParam])

  const statePassword = (location.state as LocationState | null)?.roomPassword

  const applyServerDetails = useCallback((d: RoomDetailsResponseDTO) => {
    detailsRevisionRef.current += 1
    detailsRef.current = d
    setDetails(d)
  }, [])

  const refreshRoomDetails = useCallback(async () => {
    if (!code) return
    const startRev = detailsRevisionRef.current
    try {
      const d = await getRoomByCode(code)
      if (detailsRevisionRef.current !== startRev) return
      applyServerDetails(d)
    } catch {
      // keep last known details on poll failure
    }
  }, [code, applyServerDetails])

  useEffect(() => {
    if (!code) {
      setLoading(false)
      setError('Не указан код комнаты')
      return
    }

    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      let skipLoadingOff = false
      try {
        const d = await getRoomByCode(code)
        if (cancelled) return
        applyServerDetails(d)
      } catch (e) {
        if (cancelled) return
        if (getRoomByCodeRequestHttpStatus(e) === 404) {
          skipLoadingOff = true
          navigate('/room', { replace: true, state: { roomAccessDenied: code } })
          return
        }
        setDetails(null)
        setError(e instanceof Error ? e.message : 'Не удалось загрузить комнату')
      } finally {
        if (!cancelled && !skipLoadingOff) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [code, applyServerDetails, navigate])

  useEffect(() => {
    detailsRef.current = details
  }, [details])

  const currentRoomState = details?.state
  useEffect(() => {
    if (!code) return
    if (currentRoomState === 'FINISHED') return

    let cancelled = false
    const interval = window.setInterval(() => {
      if (cancelled) return
      void refreshRoomDetails()
    }, ROOM_POLL_INTERVAL_MS)

    return () => {
      cancelled = true
      window.clearInterval(interval)
    }
  }, [code, currentRoomState, refreshRoomDetails])

  const ownerPlainPassword = useMemo(() => {
    if (!details?.currentUserIsOwner) return null
    if (statePassword) return statePassword
    if (code) return sessionStorage.getItem(roomPasswordStorageKey(code))
    return null
  }, [details?.currentUserIsOwner, statePassword, code])

  const currentUserReady = useMemo(() => {
    if (!details || !user) return null
    const self = details.participants.find((p) => p.displayName === user.displayName)
    return self?.ready ?? null
  }, [details, user])

  return {
    code,
    user,
    details,
    loading,
    error,
    applyServerDetails,
    refreshRoomDetails,
    detailsRef,
    ownerPlainPassword,
    currentUserReady,
  }
}
