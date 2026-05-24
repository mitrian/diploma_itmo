import { apiFetch, readApiErrorMessage } from './client'
import type { StageOneStatusResponseDTO, StageOneUpcomingResponseDTO } from './types'

export async function getStageOneUpcoming(
  roomCode: string,
  limit = 12,
): Promise<StageOneUpcomingResponseDTO> {
  const normalized = roomCode.trim().toUpperCase()
  const q = new URLSearchParams({ limit: String(limit) })
  const res = await apiFetch(
    `/rooms/${encodeURIComponent(normalized)}/stage-one/upcoming?${q.toString()}`,
    { method: 'GET' },
  )
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить карточки'))
  }
  return res.json() as Promise<StageOneUpcomingResponseDTO>
}

export async function getStageOneStatus(roomCode: string): Promise<StageOneStatusResponseDTO> {
  const normalized = roomCode.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/stage-one/status`, { method: 'GET' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить статус этапа 1'))
  }
  return res.json() as Promise<StageOneStatusResponseDTO>
}

export async function postStageOneVote(
  roomCode: string,
  restaurantId: number,
  suitable: boolean,
): Promise<void> {
  const normalized = roomCode.trim().toUpperCase()
  const res = await apiFetch(
    `/rooms/${encodeURIComponent(normalized)}/stage-one/votes/me/${restaurantId}`,
    {
      method: 'PUT',
      body: JSON.stringify({ suitable }),
    },
  )
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось отправить голос'))
  }
}
