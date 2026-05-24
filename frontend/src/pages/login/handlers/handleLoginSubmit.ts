import type { FormEvent } from 'react'
import type { NavigateFunction } from 'react-router-dom'

export type LoginSubmitDeps = {
  loginName: string
  password: string
  redirectTo: string
  navigate: NavigateFunction
  login: (login: string, password: string) => Promise<void>
  setError: (message: string | null) => void
  setPending: (value: boolean) => void
}

export async function handleLoginSubmit(e: FormEvent, deps: LoginSubmitDeps): Promise<void> {
  e.preventDefault()
  deps.setError(null)
  deps.setPending(true)
  try {
    await deps.login(deps.loginName.trim(), deps.password)
    deps.navigate(deps.redirectTo, { replace: true })
  } catch (err) {
    deps.setError(err instanceof Error ? err.message : 'Ошибка входа')
  } finally {
    deps.setPending(false)
  }
}
