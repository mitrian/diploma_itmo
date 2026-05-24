import { apiFetch, readApiErrorMessage } from './client'
import type { AuthResponseDTO, UserMeResponseDTO } from './types'

export async function postLogin(login: string, password: string): Promise<AuthResponseDTO> {
  const res = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ login, password }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось войти'))
  }
  return res.json() as Promise<AuthResponseDTO>
}

export async function postRegister(
  login: string,
  password: string,
  displayName: string
): Promise<AuthResponseDTO> {
  const res = await apiFetch('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ login, password, displayName }),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось зарегистрироваться'))
  }
  return res.json() as Promise<AuthResponseDTO>
}

export async function fetchCurrentUser(): Promise<UserMeResponseDTO | null> {
  const res = await apiFetch('/users/me')
  if (!res.ok) return null
  return res.json() as Promise<UserMeResponseDTO>
}

export async function postLogout(): Promise<void> {
  await apiFetch('/auth/logout', { method: 'POST' })
}
