const API_PREFIX = '/api/v1'

export function parseApiErrorMessage(bodyText: string, fallback: string): string {
  const trimmed = bodyText.trim()
  if (!trimmed) return fallback
  try {
    const parsed = JSON.parse(trimmed) as unknown
    if (
      parsed !== null &&
      typeof parsed === 'object' &&
      'message' in parsed &&
      typeof (parsed as { message: unknown }).message === 'string'
    ) {
      const m = (parsed as { message: string }).message.trim()
      return m || fallback
    }
  } catch {
    // not JSON
  }
  const legacy = trimmed.replace(/^Error:\s*/i, '').trim()
  return legacy || fallback
}

export async function readApiErrorMessage(res: Response, fallback: string): Promise<string> {
  return parseApiErrorMessage(await res.text(), fallback)
}

export function getStoredToken(): string | null {
  return localStorage.getItem('diploma_access_token')
}

export function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem('diploma_access_token', token)
  } else {
    localStorage.removeItem('diploma_access_token')
  }
}

export async function apiFetch(
  path: string,
  options: RequestInit = {}
): Promise<Response> {
  const token = getStoredToken()
  const headers = new Headers(options.headers)
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  return fetch(`${API_PREFIX}${path}`, { ...options, headers })
}

export function buildApiUrl(path: string): string {
  return `${API_PREFIX}${path}`
}
