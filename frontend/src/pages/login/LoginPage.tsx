import type { FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { handleLoginSubmit } from './handlers/handleLoginSubmit'
import { LoginPageView } from './LoginPageView'
import { useLoginPageState } from './state/useLoginPageState'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()
  const state = useLoginPageState()

  const from = (location.state as { from?: string } | null)?.from
  const redirectTo = from && from !== '/login' ? from : '/room'

  const onSubmit = (e: FormEvent) =>
    handleLoginSubmit(e, {
      loginName: state.loginName,
      password: state.password,
      redirectTo,
      navigate,
      login,
      setError: state.setError,
      setPending: state.setPending,
    })

  return (
    <LoginPageView
      loginName={state.loginName}
      password={state.password}
      error={state.error}
      pending={state.pending}
      onLoginNameChange={state.setLoginName}
      onPasswordChange={state.setPassword}
      onSubmit={onSubmit}
    />
  )
}
