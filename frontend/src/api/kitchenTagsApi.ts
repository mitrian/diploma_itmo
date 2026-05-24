import { apiFetch, readApiErrorMessage } from './client'
import type { KitchenTagDTO } from './types'

export async function getKitchenTagsCatalog(): Promise<KitchenTagDTO[]> {
  const res = await apiFetch('/kitchen-tags', { method: 'GET' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, 'Не удалось загрузить список типов кухни'))
  }
  return res.json() as Promise<KitchenTagDTO[]>
}
