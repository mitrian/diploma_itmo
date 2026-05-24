import { apiFetch, readApiErrorMessage } from './client'
import type {
  ActiveRoomResponseDTO,
  CreateRoomResponseDTO,
  JoinRoomResponseDTO,
  RoomDetailsResponseDTO,
  RoomWinnerResponseDTO,
} from './types'

/** HTTP status attached on errors thrown from {@link getRoomByCode} when {@code !res.ok}. */
export function getRoomByCodeRequestHttpStatus(err: unknown): number | undefined {
  if (typeof err === 'object' && err !== null && 'status' in err) {
    const v = (err as { status: unknown }).status
    return typeof v === 'number' ? v : undefined
  }
  return undefined
}

export async function postCreateRoom(roomPassword: string): Promise<CreateRoomResponseDTO> {
  const res = await apiFetch('/rooms', {
    method: 'POST',
    body: JSON.stringify({ roomPassword }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось создать комнату'))
  }
  return res.json() as Promise<CreateRoomResponseDTO>
}

export function roomPasswordStorageKey(roomCode: string): string {
  return `diploma_room_pwd_${roomCode}`
}

export async function getMyActiveRoom(): Promise<ActiveRoomResponseDTO> {
  const res = await apiFetch('/rooms/me/active', { method: 'GET' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось проверить активную комнату'))
  }
  return res.json() as Promise<ActiveRoomResponseDTO>
}

export async function getRoomByCode(code: string): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}`, { method: 'GET' })
  if (!res.ok) {
    const message = await readApiErrorMessage(res, 'Не удалось загрузить комнату')
    const err = new Error(message) as Error & { status: number }
    err.status = res.status
    throw err
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}

export async function getRoomWinner(code: string): Promise<RoomWinnerResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/result`, { method: 'GET' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить победителя'))
  }
  return res.json() as Promise<RoomWinnerResponseDTO>
}

export async function postJoinRoom(code: string, roomPassword: string): Promise<JoinRoomResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/participants`, {
    method: 'POST',
    body: JSON.stringify({
      roomPassword,
    }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось войти в комнату'))
  }
  return res.json() as Promise<JoinRoomResponseDTO>
}

export async function patchRoomReady(code: string, ready: boolean): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/participants/me`, {
    method: 'PATCH',
    body: JSON.stringify({ ready }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось изменить готовность'))
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}

export async function postStartSession(code: string): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}`, {
    method: 'PATCH',
    body: JSON.stringify({ state: 'STAGE_ONE' }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось начать сессию'))
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}

export async function postLeaveRoom(code: string): Promise<void> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/participants/me`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось выйти из комнаты'))
  }
}

export async function patchRoomGeoFilter(
  code: string,
  payload: { centerLat: number; centerLon: number; maxDistanceMeters: number },
): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/geo-filter`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось сохранить геофильтр'))
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}

export async function postConfirmRoomGeoFilter(code: string): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}`, {
    method: 'PATCH',
    body: JSON.stringify({ state: 'AWAITING_START' }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось подтвердить геофильтр'))
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}

export async function postRoomKitchenTags(code: string, slugs: string[]): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/kitchen-tags`, {
    method: 'POST',
    body: JSON.stringify({ slugs }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось добавить типы кухни'))
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}

export async function deleteRoomKitchenTag(code: string, slug: string): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(
    `/rooms/${encodeURIComponent(normalized)}/kitchen-tags/${encodeURIComponent(slug)}`,
    { method: 'DELETE' },
  )
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось удалить тип кухни'))
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}

export async function postConfirmKitchenFilters(code: string): Promise<RoomDetailsResponseDTO> {
  const normalized = code.trim().toUpperCase()
  const res = await apiFetch(`/rooms/${encodeURIComponent(normalized)}/kitchen-filters/lock`, {
    method: 'POST',
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось подтвердить фильтры'))
  }
  return res.json() as Promise<RoomDetailsResponseDTO>
}
