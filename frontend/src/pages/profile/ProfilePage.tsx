import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { getMyFinishedRooms } from '../../api/historyApi'
import type { RoomHistorySummaryDTO } from '../../api/types'
import { ProfilePageView } from './ProfilePageView'

type ProfileLocationState = {
  historyRoomDenied?: string
}

export function ProfilePage() {
  const location = useLocation()
  const navigate = useNavigate()
  const [items, setItems] = useState<RoomHistorySummaryDTO[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    const deniedCode = (location.state as ProfileLocationState | null)?.historyRoomDenied
    if (deniedCode) {
      setNotice(
        `История комнаты ${deniedCode} недоступна: вы не были участником этой сессии или комната не найдена.`,
      )
      navigate(location.pathname, { replace: true, state: {} })
    }
  }, [location.state, location.pathname, navigate])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    getMyFinishedRooms()
      .then((data) => {
        if (!cancelled) setItems(data)
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Не удалось загрузить историю')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  return <ProfilePageView items={items} loading={loading} error={error} notice={notice} />
}
