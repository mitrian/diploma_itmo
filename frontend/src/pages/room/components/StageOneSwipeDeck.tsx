import type { CSSProperties } from 'react'
import { useCallback, useEffect, useRef, useState } from 'react'
import { getStageOneStatus, getStageOneUpcoming, postStageOneVote } from '../../../api/stageOneApi'
import type { RestaurantCardDTO } from '../../../api/types'
import { ROOM_POLL_INTERVAL_MS } from '../../../config/polling'
import { parseServerDateTimeToEpochMs } from '../../../utils/parseServerDateTime'
import { StageOneRestaurantCardBody } from './StageOneRestaurantCardBody'

const BATCH_LIMIT = 12
const REFILL_THRESHOLD = 4
const SWIPE_COMMIT_PX = 100
const ROTATE_PER_PX = 0.028

type ExitSwipe = 'left' | 'right'

type StageOneSwipeDeckProps = {
  roomCode: string
  refreshRoomDetails: () => void | Promise<void>
}

export function StageOneSwipeDeck({ roomCode, refreshRoomDetails }: StageOneSwipeDeckProps) {
  const [cards, setCards] = useState<RestaurantCardDTO[]>([])
  const [completed, setCompleted] = useState(false)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [kickNotice, setKickNotice] = useState<string | null>(null)
  const [voteError, setVoteError] = useState<string | null>(null)
  const [voting, setVoting] = useState(false)
  const [timeoutAt, setTimeoutAt] = useState<string | null>(null)
  const [nowMs, setNowMs] = useState(() => Date.now())

  const [dragPx, setDragPx] = useState(0)
  const [isDragging, setIsDragging] = useState(false)
  const [exitSwipe, setExitSwipe] = useState<ExitSwipe | null>(null)

  const dragStartX = useRef<number | null>(null)
  const cardsRef = useRef(cards)
  const flyRestaurantIdRef = useRef<number | null>(null)
  const flySuitableRef = useRef(false)

  cardsRef.current = cards

  const fetchBatch = useCallback(async () => {
    setLoading(true)
    setLoadError(null)
    setKickNotice(null)
    try {
      const data = await getStageOneUpcoming(roomCode, BATCH_LIMIT)
      const status = await getStageOneStatus(roomCode)
      setTimeoutAt(status.timeoutAt)
      setCompleted(data.completed)
      setCards(data.cards)
    } catch (e) {
      const message = e instanceof Error ? e.message : 'Ошибка загрузки'
      if (isNotParticipantError(message)) {
        setKickNotice(
          'Вы были исключены из комнаты: до завершения первого этапа не был отправлен ни один голос.',
        )
      } else {
        setLoadError(message)
      }
      setCards([])
    } finally {
      setLoading(false)
    }
  }, [roomCode])

  useEffect(() => {
    void fetchBatch()
  }, [fetchBatch])

  useEffect(() => {
    const id = window.setInterval(() => {
      void refreshRoomDetails()
      void getStageOneStatus(roomCode)
        .then((status) => setTimeoutAt(status.timeoutAt))
        .catch((e) => {
          const message = e instanceof Error ? e.message : ''
          if (isNotParticipantError(message)) {
            setKickNotice(
              'Вы были исключены из комнаты: до завершения первого этапа не был отправлен ни один голос.',
            )
            setCards([])
          }
        })
    }, ROOM_POLL_INTERVAL_MS)
    return () => window.clearInterval(id)
  }, [refreshRoomDetails, roomCode])

  useEffect(() => {
    const id = window.setInterval(() => setNowMs(Date.now()), 1000)
    return () => window.clearInterval(id)
  }, [])

  const performVote = useCallback(
    async (restaurantId: number, suitable: boolean) => {
      if (voting) return
      setVoteError(null)
      setVoting(true)
      setDragPx(0)
      setExitSwipe(null)
      setIsDragging(false)
      try {
        await postStageOneVote(roomCode, restaurantId, suitable)
        const prev = cardsRef.current
        if (!prev.length || prev[0].id !== restaurantId) {
          const data = await getStageOneUpcoming(roomCode, BATCH_LIMIT)
          setCompleted(data.completed)
          setCards(data.cards)
          return
        }
        const next = prev.slice(1)
        if (next.length === 0 || next.length < REFILL_THRESHOLD) {
          const data = await getStageOneUpcoming(roomCode, BATCH_LIMIT)
          setCompleted(data.completed)
          setCards(data.cards)
        } else {
          setCards(next)
        }
      } catch (e) {
        setVoteError(e instanceof Error ? e.message : 'Не удалось отправить голос')
      } finally {
        setVoting(false)
      }
    },
    [voting, roomCode],
  )

  const onVoteButton = useCallback(
    (suitable: boolean) => {
      const top = cardsRef.current[0]
      if (!top || voting || exitSwipe) return
      void performVote(top.id, suitable)
    },
    [voting, exitSwipe, performVote],
  )

  const beginSwipeExit = useCallback((direction: ExitSwipe, restaurantId: number, suitable: boolean) => {
    flyRestaurantIdRef.current = restaurantId
    flySuitableRef.current = suitable
    setIsDragging(false)
    dragStartX.current = null
    setDragPx(0)
    setExitSwipe(direction)
  }, [])

  const onPointerDown = (e: React.PointerEvent) => {
    if (e.button !== 0 || voting || cards.length === 0 || exitSwipe) return
    ;(e.currentTarget as HTMLElement).setPointerCapture(e.pointerId)
    dragStartX.current = e.clientX
    setIsDragging(true)
    setDragPx(0)
  }

  const onPointerMove = (e: React.PointerEvent) => {
    if (dragStartX.current == null || exitSwipe) return
    setDragPx(e.clientX - dragStartX.current)
  }

  const resetDrag = (e: React.PointerEvent) => {
    try {
      ;(e.currentTarget as HTMLElement).releasePointerCapture(e.pointerId)
    } catch {
      /* ignore */
    }
    dragStartX.current = null
    setIsDragging(false)
    setDragPx(0)
  }

  const endDrag = (e: React.PointerEvent) => {
    if (dragStartX.current == null) return
    const dx = e.clientX - dragStartX.current
    try {
      ;(e.currentTarget as HTMLElement).releasePointerCapture(e.pointerId)
    } catch {
      /* ignore */
    }
    dragStartX.current = null
    setIsDragging(false)

    if (voting || exitSwipe) {
      setDragPx(0)
      return
    }
    const top = cardsRef.current[0]
    if (!top) {
      setDragPx(0)
      return
    }
    if (dx > SWIPE_COMMIT_PX) {
      beginSwipeExit('right', top.id, true)
    } else if (dx < -SWIPE_COMMIT_PX) {
      beginSwipeExit('left', top.id, false)
    } else {
      setDragPx(0)
    }
  }

  const onFlyTransitionEnd = useCallback(
    (ev: React.TransitionEvent<HTMLDivElement>) => {
      if (ev.target !== ev.currentTarget) return
      if (ev.propertyName !== 'transform') return
      const id = flyRestaurantIdRef.current
      if (id == null) return
      flyRestaurantIdRef.current = null
      const suitable = flySuitableRef.current
      void performVote(id, suitable)
    },
    [performVote],
  )

  const top = cards[0]

  if (loading) {
    return (
      <div className="stage-one">
        <p className="stage-one__status">Загрузка карточек…</p>
      </div>
    )
  }

  if (loadError) {
    return (
      <div className="stage-one">
        <p className="stage-one__error">{loadError}</p>
        <button type="button" className="stage-one__retry" onClick={() => void fetchBatch()}>
          Повторить
        </button>
      </div>
    )
  }

  if (kickNotice) {
    return (
      <div className="stage-one">
        <p className="stage-one__error">{kickNotice}</p>
      </div>
    )
  }

  if (completed && cards.length === 0) {
    return (
      <div className="stage-one">
        <p className="stage-one__done">Вы отметили все карточки в этом этапе.</p>
        <p className="stage-one__hint">
          Ожидайте, пока все участники завершат голосование
        </p>
      </div>
    )
  }

  const topCardClass =
    'stage-one__card stage-one__card--top' +
    (isDragging ? ' stage-one__card--top--dragging' : '') +
    (exitSwipe === 'right' ? ' stage-one__card--exit stage-one__card--exit--right' : '') +
    (exitSwipe === 'left' ? ' stage-one__card--exit stage-one__card--exit--left' : '')

  const topCardStyle: CSSProperties | undefined =
    exitSwipe != null
      ? undefined
      : {
          transform: `translateX(${dragPx}px) rotate(${dragPx * ROTATE_PER_PX}deg)`,
        }

  return (
    <div className="stage-one">
      <h2 className="room-detail__section-title">Первый этап</h2>
      <p className="stage-one__lead">
        Свайп <strong>вправо</strong> — подходит, <strong>влево</strong> — не подходит
      </p>
      {timeoutAt && (
        <p className="stage-one__status">
          До автозавершения этапа: {formatTimeoutLabel(timeoutAt, nowMs)}
        </p>
      )}

      {voteError && <p className="stage-one__error">{voteError}</p>}

      <div className={`stage-one__stack${exitSwipe ? ' stage-one__stack--exiting' : ''}`}>
        {cards.length > 1 && (
          <div className="stage-one__card stage-one__card--behind" aria-hidden>
            <div className="stage-one__card-inner">
              <h3 className="stage-one__name">{cards[1].name}</h3>
            </div>
          </div>
        )}
        {top && (
          <div
            className={topCardClass}
            style={topCardStyle}
            onPointerDown={onPointerDown}
            onPointerMove={onPointerMove}
            onPointerUp={endDrag}
            onPointerCancel={resetDrag}
            onTransitionEnd={onFlyTransitionEnd}
          >
            <div className="stage-one__card-inner">
              <StageOneRestaurantCardBody card={top} />
            </div>
          </div>
        )}
      </div>

      <div className="stage-one__actions">
        <button
          type="button"
          className="stage-one__btn stage-one__btn--reject"
          disabled={voting || !top || !!exitSwipe}
          aria-label="Не подходит"
          onClick={() => onVoteButton(false)}
        >
          ✕
        </button>
        <button
          type="button"
          className="stage-one__btn stage-one__btn--accept"
          disabled={voting || !top || !!exitSwipe}
          aria-label="Подходит"
          onClick={() => onVoteButton(true)}
        >
          ✓
        </button>
      </div>

      {voting && <p className="stage-one__status">Отправка…</p>}
    </div>
  )
}

function formatTimeoutLabel(timeoutAt: string, nowMs: number): string {
  const deadline = parseServerDateTimeToEpochMs(timeoutAt)
  if (deadline == null) return '—'
  const remainingMs = deadline - nowMs
  if (remainingMs <= 0) return '00:00'
  const totalSec = Math.floor(remainingMs / 1000)
  const mm = String(Math.floor(totalSec / 60)).padStart(2, '0')
  const ss = String(totalSec % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

function isNotParticipantError(message: string): boolean {
  const m = message.toLowerCase()
  return m.includes('not a participant') || m.includes('не участник') || m.includes('is not a participant')
}
