export function parseServerDateTimeToEpochMs(value: string | null | undefined): number | null {
  if (value == null) return null
  const s = String(value).trim()
  if (!s) return null

  const hasExplicitZone = /[zZ]$/.test(s) || /[+-]\d{2}:\d{2}$/.test(s) || /[+-]\d{4}$/.test(s)
  if (hasExplicitZone) {
    const t = Date.parse(s)
    return Number.isFinite(t) ? t : null
  }

  const trimmedFraction = s.replace(/(\.\d{3})\d*$/, '$1')
  const t = Date.parse(`${trimmedFraction}Z`)
  return Number.isFinite(t) ? t : null
}
