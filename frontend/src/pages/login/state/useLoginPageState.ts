import { useState } from 'react'

export function useLoginPageState() {
  const [loginName, setLoginName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  return {
    loginName,
    setLoginName,
    password,
    setPassword,
    error,
    setError,
    pending,
    setPending,
  }
}
