import type { FormEvent } from 'react'
import type { NavigateFunction } from 'react-router-dom'

export type RegisterSubmitDeps = {
  loginName: string
  password: string
  displayName: string
  navigate: NavigateFunction
  register: (login: string, password: string, displayName: string) => Promise<void>
  setError: (message: string | null) => void
  setPending: (value: boolean) => void
}

export async function handleRegisterSubmit(e: FormEvent, deps: RegisterSubmitDeps): Promise<void> {
  e.preventDefault()
  deps.setError(null)
  deps.setPending(true)
  try {
    await deps.register(deps.loginName.trim(), deps.password, deps.displayName.trim())
    deps.navigate('/room', { replace: true })
  } catch (err) {
    deps.setError(err instanceof Error ? err.message : 'Ошибка регистрации')
  } finally {
    deps.setPending(false)
  }
}
