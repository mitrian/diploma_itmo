import { apiFetch, readApiErrorMessage } from './client'
import type { StageTwoRankEntryDTO, StageTwoStatusResponseDTO } from './types'

export async function getStageTwoStatus(roomCode: string): Promise<StageTwoStatusResponseDTO> {
  const normalized = roomCode.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/stage-two/status`, { method: 'GET' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить статус этапа 2'))
  }
  return res.json() as Promise<StageTwoStatusResponseDTO>
}

export async function postStageTwoRanks(roomCode: string, ranks: StageTwoRankEntryDTO[]): Promise<void> {
  const normalized = roomCode.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/stage-two/ranks`, {
    method: 'POST',
    body: JSON.stringify({ ranks }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось отправить ранги этапа 2'))
  }
}
