import { useCallback, useState } from 'react'

export function useRoomEntryPageState() {
  const [createPassword, setCreatePassword] = useState('')
  const [joinCode, setJoinCode] = useState('')
  const [joinPassword, setJoinPassword] = useState('')

  const [createError, setCreateError] = useState<string | null>(null)
  const [joinError, setJoinError] = useState<string | null>(null)
  const [createPending, setCreatePending] = useState(false)
  const [joinPending, setJoinPending] = useState(false)

  const resetCreateFeedback = useCallback(() => {
    setCreateError(null)
  }, [])

  const resetJoinFeedback = useCallback(() => {
    setJoinError(null)
  }, [])

  return {
    createPassword,
    setCreatePassword,
    joinCode,
    setJoinCode,
    joinPassword,
    setJoinPassword,
    createError,
    setCreateError,
    joinError,
    setJoinError,
    createPending,
    setCreatePending,
    joinPending,
    setJoinPending,
    resetCreateFeedback,
    resetJoinFeedback,
  }
}
