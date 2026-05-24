import type { FormEvent } from 'react'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { getMyActiveRoom, postCreateRoom, postJoinRoom, roomPasswordStorageKey } from '../../api/roomsApi'
import { RoomEntryPageView } from './RoomEntryPageView'
import { useRoomEntryPageState } from './state/useRoomEntryPageState'

type RoomEntryLocationState = {
  roomAccessDenied?: string
}

export function RoomEntryPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const state = useRoomEntryPageState()
  const [checkingActiveRoom, setCheckingActiveRoom] = useState(true)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    const deniedCode = (location.state as RoomEntryLocationState | null)?.roomAccessDenied
    if (deniedCode) {
      setNotice(
        `Комната ${deniedCode}: нет доступа. Введите код и пароль ниже или создайте новую сессию.`,
      )
      navigate(location.pathname, { replace: true, state: {} })
    }
  }, [location.state, location.pathname, navigate])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const { code } = await getMyActiveRoom()
        if (cancelled || !code) return
        navigate(`/room/${encodeURIComponent(code)}`, { replace: true })
      } catch {
        //ignore
      } finally {
        if (!cancelled) setCheckingActiveRoom(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [navigate])

  const onCreateSubmit = async (e: FormEvent) => {
    e.preventDefault()
    state.resetCreateFeedback()
    state.setCreatePending(true)
    try {
      const data = await postCreateRoom(state.createPassword)
      sessionStorage.setItem(roomPasswordStorageKey(data.code), state.createPassword)
      navigate(`/room/${encodeURIComponent(data.code)}`, {
        replace: true,
        state: { roomPassword: state.createPassword },
      })
    } catch (err) {
      state.setCreateError(err instanceof Error ? err.message : 'Не удалось создать комнату')
    } finally {
      state.setCreatePending(false)
    }
  }

  const onJoinSubmit = async (e: FormEvent) => {
    e.preventDefault()
    state.resetJoinFeedback()
    state.setJoinPending(true)
    try {
      const data = await postJoinRoom(state.joinCode, state.joinPassword)
      navigate(`/room/${encodeURIComponent(data.code)}`, { replace: true })
    } catch (err) {
      state.setJoinError(err instanceof Error ? err.message : 'Не удалось войти в комнату')
    } finally {
      state.setJoinPending(false)
    }
  }

  return (
    <RoomEntryPageView
      checkingActiveRoom={checkingActiveRoom}
      notice={notice}
      createPassword={state.createPassword}
      joinCode={state.joinCode}
      joinPassword={state.joinPassword}
      createError={state.createError}
      joinError={state.joinError}
      createPending={state.createPending}
      joinPending={state.joinPending}
      onCreatePasswordChange={(v) => {
        state.setCreatePassword(v)
        state.resetCreateFeedback()
      }}
      onJoinCodeChange={(v) => {
        state.setJoinCode(v)
        state.resetJoinFeedback()
      }}
      onJoinPasswordChange={(v) => {
        state.setJoinPassword(v)
        state.resetJoinFeedback()
      }}
      onCreateSubmit={onCreateSubmit}
      onJoinSubmit={onJoinSubmit}
    />
  )
}
