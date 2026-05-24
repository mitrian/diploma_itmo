import { useCallback, useEffect, useState } from 'react'
import { getStageOneStatus } from '../../../api/stageOneApi'
import type { StageOneFinalistRowDTO, StageOneStatusResponseDTO } from '../../../api/types'
import { StageOneRestaurantCardBody } from './StageOneRestaurantCardBody'

type StageOneFinalistsPanelProps = {
  roomCode: string
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

export function StageOneFinalistsPanel({ roomCode }: StageOneFinalistsPanelProps) {
  const [status, setStatus] = useState<StageOneStatusResponseDTO | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    try {
      setLoadError(null)
      const s = await getStageOneStatus(roomCode)
      setStatus(s)
    } catch (e) {
      setLoadError(e instanceof Error ? e.message : 'Не удалось загрузить статус этапа 1')
    } finally {
      setLoading(false)
    }
  }, [roomCode])

  useEffect(() => {
    setLoading(true)
    void load()
  }, [load])

  if (loading && !status) {
    return (
      <div className="stage-one-finalists">
        <p className="stage-one-finalists__status">Загрузка финалистов…</p>
      </div>
    )
  }

  if (loadError) {
    return (
      <div className="stage-one-finalists">
        <p className="stage-one__error">{loadError}</p>
        <button type="button" className="stage-one__retry" onClick={() => void load()}>
          Повторить
        </button>
      </div>
    )
  }

  const finalists: StageOneFinalistRowDTO[] = status?.finalists ?? []

  return (
    <div className="stage-one-finalists">
      <h2 className="room-detail__section-title stage-one-finalists__title">Финалисты этапа 1</h2>
      {finalists.length === 0 ? (
        <p className="stage-one-finalists__empty">Список финалистов пуст.</p>
      ) : (
        <ul className="stage-one-finalists__list">
          {finalists.map((row) => (
            <li key={row.restaurantId} className="stage-one-finalists__row">
              <div className="stage-one-finalists__card-col">
                <div className="stage-one__card stage-one__card--finalist">
                  <div className="stage-one__card-inner">
                    <StageOneRestaurantCardBody card={row.card} />
                    <p className="stage-one-finalists__meta">
                      Место в финале: {row.position} · Одобрений: {row.approvalCount} · {inclusionLabel(row.includedBy)}
                    </p>
                  </div>
                </div>
              </div>
              <div className="stage-one-finalists__rank-col" aria-label="Ранг на втором этапе (скоро)">
                <div className="stage-one-finalists__rank-placeholder">
                  <span className="stage-one-finalists__rank-placeholder-text">Ранг</span>
                  <span className="stage-one-finalists__rank-placeholder-hint">этап 2</span>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
