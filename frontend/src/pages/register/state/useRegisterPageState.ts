import { useState } from 'react'

export function useRegisterPageState() {
  const [loginName, setLoginName] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  return {
    loginName,
    setLoginName,
    password,
    setPassword,
    displayName,
    setDisplayName,
    error,
    setError,
    pending,
    setPending,
  }
}
