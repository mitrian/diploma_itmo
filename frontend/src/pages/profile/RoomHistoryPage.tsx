import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  getRoomHistoryFilters,
  getRoomHistoryOverview,
  getRoomHistoryParticipants,
  getRoomHistoryStageOne,
  getRoomHistoryStageOneVotes,
  getRoomHistoryStageTwo,
} from '../../api/historyApi'
import { getRoomWinner } from '../../api/roomsApi'
import type {
  RoomHistoryFiltersDTO,
  RoomHistoryOverviewDTO,
  RoomHistoryParticipantDTO,
  RoomHistoryStageOneSectionDTO,
  RoomHistoryStageOneVoteDTO,
  RoomHistoryStageTwoRowDTO,
  RoomWinnerResponseDTO,
} from '../../api/types'
import { RoomHistoryPageView } from './room-history/RoomHistoryPageView'
import type { RoomHistoryErrorState, RoomHistoryLoadingState } from './room-history/RoomHistoryPageState'

export type { RoomHistoryErrorState, RoomHistoryLoadingState } from './room-history/RoomHistoryPageState'

export function RoomHistoryPage() {
  const { code } = useParams<{ code: string }>()
  const navigate = useNavigate()
  const roomCode = code ? code.trim().toUpperCase() : ''

  const [overview, setOverview] = useState<RoomHistoryOverviewDTO | null>(null)
  const [participants, setParticipants] = useState<RoomHistoryParticipantDTO[] | null>(null)
  const [filters, setFilters] = useState<RoomHistoryFiltersDTO | null>(null)
  const [stageOne, setStageOne] = useState<RoomHistoryStageOneSectionDTO | null>(null)
  const [stageTwo, setStageTwo] = useState<RoomHistoryStageTwoRowDTO[] | null>(null)
  const [winner, setWinner] = useState<RoomWinnerResponseDTO | null>(null)

  const [loading, setLoading] = useState<RoomHistoryLoadingState>({
    overview: true,
    participants: true,
    filters: true,
    stageOne: true,
    stageTwo: true,
    winner: true,
  })
  const [errors, setErrors] = useState<RoomHistoryErrorState>({
    overview: null,
    participants: null,
    filters: null,
    stageOne: null,
    stageTwo: null,
    winner: null,
  })

  const [stageOneVotes, setStageOneVotes] = useState<Record<number, RoomHistoryStageOneVoteDTO[]>>({})
  const [stageOneVotesLoading, setStageOneVotesLoading] = useState<Record<number, boolean>>({})
  const [stageOneVotesError, setStageOneVotesError] = useState<Record<number, string | null>>({})

  useEffect(() => {
    if (!roomCode) {
      navigate('/profile', { replace: true })
      return
    }
    let cancelled = false

    setOverview(null)
    setParticipants(null)
    setFilters(null)
    setStageOne(null)
    setStageTwo(null)
    setWinner(null)
    setStageOneVotes({})
    setStageOneVotesLoading({})
    setStageOneVotesError({})
    setErrors({
      overview: null,
      participants: null,
      filters: null,
      stageOne: null,
      stageTwo: null,
      winner: null,
    })
    setLoading({
      overview: true,
      participants: true,
      filters: true,
      stageOne: true,
      stageTwo: true,
      winner: true,
    })

    function setLoadingPartial(key: keyof RoomHistoryLoadingState, value: boolean) {
      if (cancelled) return
      setLoading((prev) => ({ ...prev, [key]: value }))
    }
    function setErrorPartial(key: keyof RoomHistoryErrorState, value: string | null) {
      if (cancelled) return
      setErrors((prev) => ({ ...prev, [key]: value }))
    }

    async function load<T>(
      key: keyof RoomHistoryLoadingState,
      action: () => Promise<T>,
      apply: (value: T) => void,
    ): Promise<void> {
      setLoadingPartial(key, true)
      setErrorPartial(key, null)
      try {
        const result = await action()
        if (!cancelled) apply(result)
      } catch (err) {
        if (!cancelled) {
          setErrorPartial(key, err instanceof Error ? err.message : 'Ошибка загрузки')
        }
      } finally {
        setLoadingPartial(key, false)
      }
    }

    async function run(): Promise<void> {
      setLoadingPartial('overview', true)
      setErrorPartial('overview', null)
      try {
        const overviewData = await getRoomHistoryOverview(roomCode)
        if (cancelled) return
        setOverview(overviewData)
        setErrorPartial('overview', null)
      } catch {
        if (!cancelled) {
          navigate('/profile', { replace: true, state: { historyRoomDenied: roomCode } })
        }
        return
      } finally {
        if (!cancelled) setLoadingPartial('overview', false)
      }

      if (cancelled) return

      await Promise.allSettled([
        load('participants', () => getRoomHistoryParticipants(roomCode), setParticipants),
        load('filters', () => getRoomHistoryFilters(roomCode), setFilters),
        load('stageOne', () => getRoomHistoryStageOne(roomCode), setStageOne),
        load('stageTwo', () => getRoomHistoryStageTwo(roomCode), setStageTwo),
        load('winner', () => getRoomWinner(roomCode), setWinner),
      ])
    }

    void run()

    return () => {
      cancelled = true
    }
  }, [roomCode, navigate])

  async function handleLoadStageOneVotes(restaurantId: number): Promise<void> {
    if (!roomCode) return
    if (stageOneVotes[restaurantId] || stageOneVotesLoading[restaurantId]) return
    setStageOneVotesLoading((prev) => ({ ...prev, [restaurantId]: true }))
    setStageOneVotesError((prev) => ({ ...prev, [restaurantId]: null }))
    try {
      const data = await getRoomHistoryStageOneVotes(roomCode, restaurantId)
      setStageOneVotes((prev) => ({ ...prev, [restaurantId]: data }))
    } catch (err) {
      setStageOneVotesError((prev) => ({
        ...prev,
        [restaurantId]: err instanceof Error ? err.message : 'Ошибка загрузки голосов',
      }))
    } finally {
      setStageOneVotesLoading((prev) => ({ ...prev, [restaurantId]: false }))
    }
  }

  return (
    <RoomHistoryPageView
      roomCode={roomCode}
      overview={overview}
      participants={participants}
      filters={filters}
      stageOne={stageOne}
      stageTwo={stageTwo}
      winner={winner}
      loading={loading}
      errors={errors}
      stageOneVotes={stageOneVotes}
      stageOneVotesLoading={stageOneVotesLoading}
      stageOneVotesError={stageOneVotesError}
      onLoadStageOneVotes={handleLoadStageOneVotes}
    />
  )
}
