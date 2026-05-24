import { useCallback, useEffect, useMemo, useState } from 'react'
import { getStageOneStatus } from '../../../api/stageOneApi'
import { getStageTwoStatus, postStageTwoRanks } from '../../../api/stageTwoApi'
import type { RestaurantCardDTO, StageTwoRankEntryDTO, StageTwoStatusResponseDTO } from '../../../api/types'
import { ROOM_POLL_INTERVAL_MS } from '../../../config/polling'
import { parseServerDateTimeToEpochMs } from '../../../utils/parseServerDateTime'
import { StageOneRestaurantCardBody } from './StageOneRestaurantCardBody'

type StageTwoRankingPanelProps = {
  roomCode: string
  refreshRoomDetails: () => void | Promise<void>
}

function inclusionLabel(includedBy: string): string {
  switch (includedBy) {
    case 'BASE_QUORUM':
      return 'базовый кворум'
    case 'RELAXED_QUORUM':
      return 'ослабленный кворум'
    case 'ORGANIZER_DOUBLE':
      return 'резерв: вес голоса организатора ×2'
    default:
      return includedBy
  }
}

type FinalistRow = {
  restaurantId: number
  position: number
  approvalCount: number
  includedBy: string
  card: RestaurantCardDTO | null
}

export function StageTwoRankingPanel({ roomCode, refreshRoomDetails }: StageTwoRankingPanelProps) {
  const [status, setStatus] = useState<StageTwoStatusResponseDTO | null>(null)
  const [finalists, setFinalists] = useState<FinalistRow[]>([])
  const [rankByRestaurant, setRankByRestaurant] = useState<Record<number, number | undefined>>({})
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [submitSuccess, setSubmitSuccess] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [nowMs, setNowMs] = useState(() => Date.now())

  const load = useCallback(async (): Promise<StageTwoStatusResponseDTO | null> => {
    setLoadError(null)
    try {
      const [s1, s2] = await Promise.all([getStageOneStatus(roomCode), getStageTwoStatus(roomCode)])
      const cardsByRestaurant = new Map<number, RestaurantCardDTO>()
      for (const row of s1.finalists) {
        cardsByRestaurant.set(row.restaurantId, row.card)
      }
      const merged = [...s2.finalists]
        .sort((a, b) => a.position - b.position)
        .map((row) => ({
          restaurantId: row.restaurantId,
          position: row.position,
          approvalCount: row.approvalCount,
          includedBy: row.includedBy,
          card: cardsByRestaurant.get(row.restaurantId) ?? null,
        }))

      setStatus(s2)
      setFinalists(merged)
      setRankByRestaurant((prev) => {
        const nextRanks: Record<number, number | undefined> = {}
        if (s2.myRanks.length > 0) {
          for (const row of s2.myRanks) {
            nextRanks[row.restaurantId] = row.rank
          }
        } else {
          for (const row of merged) {
            nextRanks[row.restaurantId] = prev[row.restaurantId]
          }
        }
        return nextRanks
      })
      if (s2.roomState === 'FINISHED') {
        void refreshRoomDetails()
      }
      return s2
    } catch (e) {
      setLoadError(e instanceof Error ? e.message : 'Не удалось загрузить этап 2')
      return null
    } finally {
      setLoading(false)
    }
  }, [roomCode, refreshRoomDetails])

  useEffect(() => {
    setLoading(true)
    void load()
  }, [load])

  useEffect(() => {
    if (!status || status.roomState !== 'STAGE_TWO') return
    const intervalId = window.setInterval(() => {
      void load()
    }, ROOM_POLL_INTERVAL_MS)
    return () => {
      window.clearInterval(intervalId)
    }
  }, [status, load])

  useEffect(() => {
    const id = window.setInterval(() => setNowMs(Date.now()), 1000)
    return () => window.clearInterval(id)
  }, [])

  const finalistCount = finalists.length
  const rankOptions = useMemo(
    () => Array.from({ length: finalistCount }, (_, i) => i + 1),
    [finalistCount],
  )

  const selectedRankCounts = useMemo(() => {
    const counts = new Map<number, number>()
    for (const value of Object.values(rankByRestaurant)) {
      if (typeof value !== 'number') continue
      counts.set(value, (counts.get(value) ?? 0) + 1)
    }
    return counts
  }, [rankByRestaurant])

  const handleRankChange = (restaurantId: number, rank: number | undefined) => {
    setSubmitError(null)
    setSubmitSuccess(null)
    setRankByRestaurant((prev) => ({ ...prev, [restaurantId]: rank }))
  }

  const onSubmit = useCallback(async () => {
    if (!status || finalistCount < 2) return
    setSubmitError(null)
    setSubmitSuccess(null)

    const entries: StageTwoRankEntryDTO[] = finalists.map((row) => ({
      restaurantId: row.restaurantId,
      rank: rankByRestaurant[row.restaurantId] ?? 0,
    }))
    const usedRanks = new Set(entries.map((e) => e.rank))
    if (entries.some((e) => e.rank < 1 || e.rank > finalistCount) || usedRanks.size !== finalistCount) {
      setSubmitError('Нужно выставить уникальные ранги от 1 до N для всех финалистов.')
      return
    }

    setSubmitting(true)
    try {
      await postStageTwoRanks(roomCode, entries)
      const afterSubmit = await load()
      if (afterSubmit?.roomState === 'STAGE_TWO') {
        setSubmitSuccess('Ваши ранги сохранены. Ждём, пока остальные участники завершат ранжирование.')
      }
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : 'Не удалось отправить ранги')
    } finally {
      setSubmitting(false)
    }
  }, [finalistCount, finalists, load, rankByRestaurant, roomCode, status])

  if (loading && !status) {
    return <p className="stage-one-finalists__status">Загрузка этапа 2…</p>
  }
  if (loadError) {
    return (
      <div>
        <p className="stage-one__error">{loadError}</p>
        <button type="button" className="stage-one__retry" onClick={() => void load()}>
          Повторить
        </button>
      </div>
    )
  }
  if (!status) return null

  const votingClosed = status.roomState === 'FINISHED'
  const alreadyVoted = status.myRanks.length > 0
  const readOnly = submitting || votingClosed || alreadyVoted
  const timeoutLabel = formatTimeoutLabel(status.timeoutAt, nowMs)
  return (
    <div className="stage-two-ranking">
      <h2 className="room-detail__section-title stage-one-finalists__title">Ранжирование этапа 2</h2>
      <p className="stage-one__lead">
        Расставьте уникальные ранги для всех финалистов: 1 — лучший вариант, {finalistCount} — наименее
        предпочтительный.
      </p>
      {timeoutLabel && <p className="stage-one__status">До автозавершения этапа: {timeoutLabel}</p>}
      <ul className="stage-one-finalists__list stage-two-ranking__list">
        {finalists.map((row) => (
          <li key={row.restaurantId} className="stage-one-finalists__row stage-two-ranking__row">
            <div className="stage-one-finalists__card-col stage-two-ranking__card-col">
              <div className="stage-one__card stage-one__card--finalist stage-two-ranking__card">
                <div className="stage-one__card-inner stage-two-ranking__card-inner">
                  <div className="stage-two-ranking__title-row">
                    <div className="stage-two-ranking__title-col">
                      {row.card ? (
                        <h3 className="stage-one__name">{row.card.name}</h3>
                      ) : (
                        <p className="stage-two-ranking__name-fallback">Карточка ресторана недоступна.</p>
                      )}
                    </div>
                    <div className="stage-two-ranking__inline-rank">
                      <label className="stage-two-ranking__rank-label">
                        Ранг
                        <select
                          className="stage-two-ranking__select"
                          value={rankByRestaurant[row.restaurantId] ?? ''}
                          onChange={(e) =>
                            handleRankChange(
                              row.restaurantId,
                              e.target.value === '' ? undefined : Number(e.target.value),
                            )
                          }
                          disabled={readOnly}
                        >
                          <option value="">-</option>
                          {rankOptions.map((r) => (
                            <option
                              key={r}
                              value={r}
                              disabled={
                                rankByRestaurant[row.restaurantId] !== r && (selectedRankCounts.get(r) ?? 0) > 0
                              }
                            >
                              {r}
                            </option>
                          ))}
                        </select>
                      </label>
                    </div>
                  </div>
                  {row.card ? <StageOneRestaurantCardBody card={row.card} omitName /> : null}
                  <p className="stage-one-finalists__meta">
                    Место в финале: {row.position} · Одобрений: {row.approvalCount} · {inclusionLabel(row.includedBy)}
                  </p>
                </div>
              </div>
            </div>
          </li>
        ))}
      </ul>

      {!votingClosed && !alreadyVoted && (
        <button type="button" className="room-detail__btn room-detail__btn--primary" onClick={() => void onSubmit()} disabled={submitting}>
          {submitting ? 'Сохраняем…' : 'Отправить ранги'}
        </button>
      )}
      {submitError && <p className="room-detail__ready-error">{submitError}</p>}
      {submitSuccess && <p className="stage-two-ranking__success">{submitSuccess}</p>}
    </div>
  )
}

function formatTimeoutLabel(timeoutAt: string | null, nowMs: number): string | null {
  if (!timeoutAt) return null
  const deadline = parseServerDateTimeToEpochMs(timeoutAt)
  if (deadline == null) return null
  const remainingMs = deadline - nowMs
  if (remainingMs <= 0) return '00:00'
  const totalSec = Math.floor(remainingMs / 1000)
  const mm = String(Math.floor(totalSec / 60)).padStart(2, '0')
  const ss = String(totalSec % 60).padStart(2, '0')
  return `${mm}:${ss}`
}
