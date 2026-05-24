const DEFAULT_ROOM_POLL_INTERVAL_MS = 3000
const MIN_ROOM_POLL_INTERVAL_MS = 1000

function readIntervalFromEnv(): number | null {
  const raw = import.meta.env.VITE_ROOM_POLL_INTERVAL_MS
  if (raw === undefined || raw === '') return null
  const n = Number(raw)
  return Number.isFinite(n) ? n : null
}

export const ROOM_POLL_INTERVAL_MS = (() => {
  const fromEnv = readIntervalFromEnv()
  const base = fromEnv ?? DEFAULT_ROOM_POLL_INTERVAL_MS
  return Math.max(MIN_ROOM_POLL_INTERVAL_MS, base)
})()
