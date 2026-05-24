import type { WinnerSelectionPrinciple } from '../../../api/types'

export function formatVotingDuration(seconds: number | null | undefined): string {
  if (seconds == null || seconds < 0 || !Number.isFinite(seconds)) {
    return '—'
  }
  const total = Math.floor(seconds)
  const minutes = Math.floor(total / 60)
  const secs = total % 60
  return `${minutes}:${String(secs).padStart(2, '0')}`
}

export function formatDateTime(iso: string): string {
  try {
    return new Date(iso).toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return iso
  }
}

export function principleLabel(p: WinnerSelectionPrinciple): string {
  switch (p) {
    case 'NONE':
      return 'Победитель не определён'
    case 'STAGE_ONE_BASE_QUORUM_SINGLE':
      return 'Победитель определён ещё на этапе 1: единственный кандидат набрал строгий кворум'
    case 'STAGE_TWO_RANK_SUM_UNIQUE_LEADER':
      return 'Победитель определён по сумме рангов на этапе 2 (явный лидер)'
    case 'STAGE_TWO_TIEBREAK_BY_APPROVAL_COUNT':
      return 'Тай-брейк: победил кандидат с большим числом одобрений на этапе 1'
    case 'STAGE_TWO_TIEBREAK_BY_ORGANIZER_RANK':
      return 'Тай-брейк: победил кандидат с лучшим рангом организатора (резерв с приоритетом голоса организатора)'
    case 'STAGE_TWO_TIEBREAK_BY_STAGE_ONE_POSITION':
      return 'Тай-брейк: победил кандидат с более ранней позицией среди финалистов'
    default:
      return p
  }
}

export function stageTwoEmptyExplanation(
  winnerPrinciple: WinnerSelectionPrinciple,
  chosenRestaurantId: number | null,
): string {
  const finishedAtStageOneOnly =
    winnerPrinciple === 'STAGE_ONE_BASE_QUORUM_SINGLE' && chosenRestaurantId != null
  if (finishedAtStageOneOnly) {
    return 'Этап 2 не проводился. Победитель определён на этапе 1 (единственный кандидат набрал строгий кворум).'
  }
  if (chosenRestaurantId == null) {
    return 'Данных по рангам этапа 2 нет. Победитель по итогам сессии не выбран'
  }
  return 'Данных по рангам этапа 2 нет. Итог по победителю см. в блоке «Победитель» выше.'
}

export function inclusionLabel(includedBy: string | null | undefined): string | null {
  if (!includedBy) return null
  switch (includedBy) {
    case 'BASE_QUORUM':
      return 'Базовый кворум'
    case 'RELAXED_QUORUM':
      return 'Слабый кворум'
    case 'ORGANIZER_DOUBLE':
      return 'Удвоение голоса организатора'
    default:
      return includedBy
  }
}
