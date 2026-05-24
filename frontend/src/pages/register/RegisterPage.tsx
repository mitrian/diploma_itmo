import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { handleRegisterSubmit } from './handlers/handleRegisterSubmit'
import { RegisterPageView } from './RegisterPageView'
import { useRegisterPageState } from './state/useRegisterPageState'

export function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const state = useRegisterPageState()

  const onSubmit = (e: FormEvent) =>
    handleRegisterSubmit(e, {
      loginName: state.loginName,
      password: state.password,
      displayName: state.displayName,
      navigate,
      register,
      setError: state.setError,
      setPending: state.setPending,
    })

  return (
    <RegisterPageView
      loginName={state.loginName}
      displayName={state.displayName}
      password={state.password}
      error={state.error}
      pending={state.pending}
      onLoginNameChange={state.setLoginName}
      onDisplayNameChange={state.setDisplayName}
      onPasswordChange={state.setPassword}
      onSubmit={onSubmit}
    />
  )
}
