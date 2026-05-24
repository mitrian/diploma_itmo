import { apiFetch, readApiErrorMessage } from './client'
import {
  type RoomHistoryFiltersDTO,
  type RoomHistoryOverviewDTO,
  type RoomHistoryParticipantDTO,
  type RoomHistoryStageOneSectionDTO,
  type RoomHistoryStageOneVoteDTO,
  type RoomHistoryStageTwoRowDTO,
  type RoomHistorySummaryDTO,
  type WinnerSelectionPrinciple,
  isWinnerSelectionPrinciple,
} from './types'

function normalizeCode(code: string): string {
  return code.trim().toUpperCase()
}

function normalizeOverviewWinnerPrinciple(raw: RoomHistoryOverviewDTO): RoomHistoryOverviewDTO {
  const v = raw.winnerPrinciple
  const s = typeof v === 'string' ? v.trim() : ''
  const winnerPrinciple: WinnerSelectionPrinciple = isWinnerSelectionPrinciple(s) ? s : 'NONE'
  if (winnerPrinciple === raw.winnerPrinciple) {
    return raw
  }
  return { ...raw, winnerPrinciple }
}

export async function getMyFinishedRooms(): Promise<RoomHistorySummaryDTO[]> {
  const res = await apiFetch('/rooms/me/history', { method: 'GET' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить историю комнат'))
  }
  return res.json() as Promise<RoomHistorySummaryDTO[]>
}

export async function getRoomHistoryOverview(code: string): Promise<RoomHistoryOverviewDTO> {
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalizeCode(code))}/history/overview`, {
    method: 'GET',
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить обзор истории'))
  }
  const body = (await res.json()) as RoomHistoryOverviewDTO
  return normalizeOverviewWinnerPrinciple(body)
}

export async function getRoomHistoryParticipants(code: string): Promise<RoomHistoryParticipantDTO[]> {
  const res = await apiFetch(
    `/rooms/${encodeURIComponent(normalizeCode(code))}/history/participants`,
    { method: 'GET' },
  )
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить участников'))
  }
  return res.json() as Promise<RoomHistoryParticipantDTO[]>
}

export async function getRoomHistoryFilters(code: string): Promise<RoomHistoryFiltersDTO> {
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalizeCode(code))}/history/filters`, {
    method: 'GET',
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить фильтры'))
  }
  return res.json() as Promise<RoomHistoryFiltersDTO>
}

export async function getRoomHistoryStageOne(code: string): Promise<RoomHistoryStageOneSectionDTO> {
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalizeCode(code))}/history/stage-one`, {
    method: 'GET',
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить этап 1'))
  }
  return res.json() as Promise<RoomHistoryStageOneSectionDTO>
}

export async function getRoomHistoryStageOneVotes(
  code: string,
  restaurantId: number,
): Promise<RoomHistoryStageOneVoteDTO[]> {
  const res = await apiFetch(
    `/rooms/${encodeURIComponent(normalizeCode(code))}/history/stage-one/restaurants/${restaurantId}/votes`,
    { method: 'GET' },
  )
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить голоса по ресторану'))
  }
  return res.json() as Promise<RoomHistoryStageOneVoteDTO[]>
}

export async function getRoomHistoryStageTwo(code: string): Promise<RoomHistoryStageTwoRowDTO[]> {
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalizeCode(code))}/history/stage-two`, {
    method: 'GET',
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить этап 2'))
  }
  return res.json() as Promise<RoomHistoryStageTwoRowDTO[]>
}
