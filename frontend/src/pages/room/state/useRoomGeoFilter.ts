import { useCallback, useEffect, useRef, useState } from 'react'
import { patchRoomGeoFilter, postConfirmRoomGeoFilter } from '../../../api/roomsApi'
import type { RoomDetailsResponseDTO } from '../../../api/types'
import {
  DEFAULT_CATALOG_CENTER_LAT,
  DEFAULT_CATALOG_CENTER_LON,
  DEFAULT_CATALOG_RADIUS_METERS,
} from '../../../config/catalogGeoDefaults'

type ApplyDetails = (d: RoomDetailsResponseDTO) => void

export function useRoomGeoFilter(
  code: string,
  details: RoomDetailsResponseDTO | null,
  applyServerDetails: ApplyDetails,
) {
  const [geoCenterLat, setGeoCenterLat] = useState('')
  const [geoCenterLon, setGeoCenterLon] = useState('')
  const [geoMaxDistanceMeters, setGeoMaxDistanceMeters] = useState('')
  const [geoSaving, setGeoSaving] = useState(false)
  const [geoError, setGeoError] = useState<string | null>(null)
  const [geoConfirmSaving, setGeoConfirmSaving] = useState(false)
  const [geoConfirmError, setGeoConfirmError] = useState<string | null>(null)

  const geoInitForRoomRef = useRef<string | null>(null)
  const prevRoomStateRef = useRef<string | null>(null)

  useEffect(() => {
    const s = details?.state ?? null
    if (s === 'GEO_FILTERS' && prevRoomStateRef.current !== 'GEO_FILTERS') {
      geoInitForRoomRef.current = null
    }
    prevRoomStateRef.current = s
  }, [details?.state])

  useEffect(() => {
    if (!details || details.state !== 'GEO_FILTERS') {
      geoInitForRoomRef.current = null
      return
    }
    if (geoInitForRoomRef.current === code) return
    geoInitForRoomRef.current = code
    setGeoCenterLat(details.centerLat == null ? DEFAULT_CATALOG_CENTER_LAT : String(details.centerLat))
    setGeoCenterLon(details.centerLon == null ? DEFAULT_CATALOG_CENTER_LON : String(details.centerLon))
    setGeoMaxDistanceMeters(
      details.maxDistanceMeters == null ? DEFAULT_CATALOG_RADIUS_METERS : String(details.maxDistanceMeters),
    )
  }, [details, code])

  const onSaveGeoFilter = useCallback(async () => {
    if (!code) return
    setGeoError(null)
    setGeoConfirmError(null)
    const centerLat = Number(geoCenterLat)
    const centerLon = Number(geoCenterLon)
    const maxDistanceMeters = Number(geoMaxDistanceMeters)
    if (!Number.isFinite(centerLat) || !Number.isFinite(centerLon) || !Number.isFinite(maxDistanceMeters)) {
      setGeoError('Введите корректные числовые значения геофильтра')
      return
    }

    setGeoSaving(true)
    try {
      const d = await patchRoomGeoFilter(code, { centerLat, centerLon, maxDistanceMeters })
      applyServerDetails(d)
      setGeoConfirmError(null)
    } catch (e) {
      setGeoError(e instanceof Error ? e.message : 'Не удалось сохранить геофильтр')
    } finally {
      setGeoSaving(false)
    }
  }, [code, geoCenterLat, geoCenterLon, geoMaxDistanceMeters, applyServerDetails])

  const onConfirmGeoFilter = useCallback(async () => {
    if (!code) return
    setGeoConfirmError(null)
    setGeoConfirmSaving(true)
    try {
      const d = await postConfirmRoomGeoFilter(code)
      applyServerDetails(d)
    } catch (e) {
      setGeoConfirmError(e instanceof Error ? e.message : 'Не удалось подтвердить геофильтр')
    } finally {
      setGeoConfirmSaving(false)
    }
  }, [code, applyServerDetails])

  const onGeoCenterLatInputChange = useCallback((value: string) => {
    setGeoCenterLat(value)
    setGeoConfirmError(null)
  }, [])

  const onGeoCenterLonInputChange = useCallback((value: string) => {
    setGeoCenterLon(value)
    setGeoConfirmError(null)
  }, [])

  const onGeoMaxDistanceMetersInputChange = useCallback((value: string) => {
    setGeoMaxDistanceMeters(value)
    setGeoConfirmError(null)
  }, [])

  return {
    geoCenterLat,
    geoCenterLon,
    geoMaxDistanceMeters,
    onGeoCenterLatChange: onGeoCenterLatInputChange,
    onGeoCenterLonChange: onGeoCenterLonInputChange,
    onGeoMaxDistanceMetersChange: onGeoMaxDistanceMetersInputChange,
    geoSaving,
    geoError,
    onSaveGeoFilter,
    geoConfirmSaving,
    geoConfirmError,
    onConfirmGeoFilter,
  }
}
