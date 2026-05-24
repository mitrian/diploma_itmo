import { useCallback, useEffect, useMemo, useState } from 'react'
import { getKitchenTagsCatalog } from '../../../api/kitchenTagsApi'
import {
  deleteRoomKitchenTag,
  postConfirmKitchenFilters,
  postRoomKitchenTags,
} from '../../../api/roomsApi'
import type { KitchenTagDTO, RoomDetailsResponseDTO } from '../../../api/types'
import { useAuth } from '../../../context/AuthContext'

type ApplyDetails = (d: RoomDetailsResponseDTO) => void

export function useRoomKitchenFilters(
  code: string,
  details: RoomDetailsResponseDTO | null,
  applyServerDetails: ApplyDetails,
) {
  const { user } = useAuth()

  const [kitchenCatalog, setKitchenCatalog] = useState<KitchenTagDTO[] | null>(null)
  const [kitchenCatalogLoading, setKitchenCatalogLoading] = useState(false)
  const [kitchenCatalogError, setKitchenCatalogError] = useState<string | null>(null)
  const [pendingKitchenSlugs, setPendingKitchenSlugs] = useState<string[]>([])
  const [kitchenAddSaving, setKitchenAddSaving] = useState(false)
  const [kitchenRemoveSlug, setKitchenRemoveSlug] = useState<string | null>(null)
  const [kitchenConfirmSaving, setKitchenConfirmSaving] = useState(false)
  const [kitchenActionError, setKitchenActionError] = useState<string | null>(null)

  useEffect(() => {
    if (details?.state !== 'AWAITING_START') {
      setKitchenCatalog(null)
      setKitchenCatalogError(null)
      setPendingKitchenSlugs([])
      return
    }
    let cancelled = false
    ;(async () => {
      setKitchenCatalogLoading(true)
      setKitchenCatalogError(null)
      try {
        const list = await getKitchenTagsCatalog()
        if (!cancelled) setKitchenCatalog(list)
      } catch (e) {
        if (!cancelled) {
          setKitchenCatalog(null)
          setKitchenCatalogError(e instanceof Error ? e.message : 'Не удалось загрузить справочник')
        }
      } finally {
        if (!cancelled) setKitchenCatalogLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [details?.state])

  const kitchenFiltersLocked = useMemo(() => {
    if (!details?.participants?.length) return false
    return details.participants.every((p) => p.filtersConfirmed === true)
  }, [details])

  const currentUserFiltersConfirmed = useMemo(() => {
    if (!details || !user) return false
    return details.participants.find((p) => p.displayName === user.displayName)?.filtersConfirmed === true
  }, [details, user])

  const getKitchenTagLabel = useCallback(
    (slug: string) => {
      const fromCat = kitchenCatalog?.find((t) => t.slug === slug)
      if (fromCat) return fromCat.labelRu
      const fromRoom = details?.roomKitchenTags?.find((t) => t.slug === slug)
      return fromRoom?.labelRu ?? slug
    },
    [kitchenCatalog, details],
  )

  const onAddKitchenTags = useCallback(async () => {
    if (!code || pendingKitchenSlugs.length === 0) return
    setKitchenActionError(null)
    setKitchenAddSaving(true)
    try {
      const d = await postRoomKitchenTags(code, pendingKitchenSlugs)
      applyServerDetails(d)
      setPendingKitchenSlugs([])
    } catch (e) {
      setKitchenActionError(e instanceof Error ? e.message : 'Не удалось добавить типы кухни')
    } finally {
      setKitchenAddSaving(false)
    }
  }, [code, pendingKitchenSlugs, applyServerDetails])

  const onRemoveKitchenTag = useCallback(
    async (slug: string) => {
      if (!code) return
      setKitchenActionError(null)
      setKitchenRemoveSlug(slug)
      try {
        const d = await deleteRoomKitchenTag(code, slug)
        applyServerDetails(d)
      } catch (e) {
        setKitchenActionError(e instanceof Error ? e.message : 'Не удалось удалить тип кухни')
      } finally {
        setKitchenRemoveSlug(null)
      }
    },
    [code, applyServerDetails],
  )

  const onConfirmKitchenFilters = useCallback(async () => {
    if (!code) return
    setKitchenActionError(null)
    setKitchenConfirmSaving(true)
    try {
      const d = await postConfirmKitchenFilters(code)
      applyServerDetails(d)
    } catch (e) {
      setKitchenActionError(e instanceof Error ? e.message : 'Не удалось подтвердить фильтры')
    } finally {
      setKitchenConfirmSaving(false)
    }
  }, [code, applyServerDetails])

  return {
    kitchenCatalog,
    kitchenCatalogLoading,
    kitchenCatalogError,
    pendingKitchenSlugs,
    setPendingKitchenSlugs,
    kitchenAddSaving,
    kitchenRemoveSlug,
    kitchenConfirmSaving,
    kitchenActionError,
    kitchenFiltersLocked,
    currentUserFiltersConfirmed,
    getKitchenTagLabel,
    onAddKitchenTags,
    onRemoveKitchenTag,
    onConfirmKitchenFilters,
  }
}
