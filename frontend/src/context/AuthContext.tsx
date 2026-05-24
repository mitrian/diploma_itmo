import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type { AuthResponseDTO } from '../api/types'
import { fetchCurrentUser, postLogin, postLogout, postRegister } from '../api/authApi'
import { getStoredToken, setStoredToken } from '../api/client'

type AuthUser = {
  login: string
  displayName: string
}

type AuthContextValue = {
  user: AuthUser | null
  loading: boolean
  login: (login: string, password: string) => Promise<void>
  register: (login: string, password: string, displayName: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(() => Boolean(getStoredToken()))

  const logout = useCallback(() => {
    void postLogout()
    setStoredToken(null)
    setUser(null)
  }, [])

  const applyAuthResponse = useCallback((data: AuthResponseDTO, loginHint?: string) => {
    setStoredToken(data.token)
    setUser({
      login: loginHint ?? '',
      displayName: data.displayName,
    })
  }, [])

  useEffect(() => {
    const token = getStoredToken()
    if (!token) {
      setLoading(false)
      return
    }

    let cancelled = false
    ;(async () => {
      try {
        const me = await fetchCurrentUser()
        if (!me) {
          if (!cancelled) {
            setStoredToken(null)
            setUser(null)
          }
          return
        }
        if (!cancelled) {
          setUser({ login: me.login, displayName: me.displayName })
        }
      } catch {
        if (!cancelled) {
          setStoredToken(null)
          setUser(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(
    async (loginName: string, password: string) => {
      setStoredToken(null)
      const data = await postLogin(loginName, password)
      applyAuthResponse(data, loginName)
    },
    [applyAuthResponse]
  )

  const register = useCallback(
    async (loginName: string, password: string, displayName: string) => {
      setStoredToken(null)
      const data = await postRegister(loginName, password, displayName)
      applyAuthResponse(data, loginName)
    },
    [applyAuthResponse]
  )

  const value = useMemo(
    () => ({
      user,
      loading,
      login,
      register,
      logout,
    }),
    [user, loading, login, register, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
