import { useCallback, useEffect, useState } from 'react'
import { getRoomWinner } from '../../../api/roomsApi'
import type { RoomWinnerResponseDTO } from '../../../api/types'
import { StageOneRestaurantCardBody } from './StageOneRestaurantCardBody'

type StageOneFinishedWinnerProps = {
  roomCode: string
}

export function StageOneFinishedWinner({ roomCode }: StageOneFinishedWinnerProps) {
  const [winner, setWinner] = useState<RoomWinnerResponseDTO | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    try {
      setLoadError(null)
      setWinner(await getRoomWinner(roomCode))
    } catch (e) {
      setLoadError(e instanceof Error ? e.message : 'Не удалось загрузить итог')
    } finally {
      setLoading(false)
    }
  }, [roomCode])

  useEffect(() => {
    void load()
  }, [load])

  if (loading && !status) {
    return (
      <div className="stage-one-finished">
        <p className="stage-one-finalists__status">Загрузка итога…</p>
      </div>
    )
  }

  if (loadError) {
    return (
      <div className="stage-one-finished">
        <p className="stage-one__error">{loadError}</p>
        <button type="button" className="stage-one__retry" onClick={() => void load()}>
          Повторить
        </button>
      </div>
    )
  }

  const winnerCard = winner?.winnerRestaurant ?? null

  if (!winnerCard) {
    return (
      <div className="stage-one-finished">
        <h2 className="room-detail__section-title stage-one-finished__title">Итог голосования</h2>
        <p className="stage-one-finished__lead">
          К сожалению, нам не удалось подобрать для вашей компании подходящий вариант.
        </p>
      </div>
    )
  }

  if (winnerCard) {
    return (
      <div className="stage-one-finished">
        <p className="stage-one-finished__success">Вы успешно выбрали место</p>
        <div className="stage-one-finished__card-wrap">
          <div className="stage-one__card stage-one__card--finalist stage-one__card--finished-winner">
            <div className="stage-one__card-inner">
              <StageOneRestaurantCardBody card={winnerCard} />
            </div>
          </div>
        </div>
      </div>
    )
  }
}
