import type { KitchenTagDTO, RoomDetailsResponseDTO } from '../../api/types'

export type RoomPageSummaryProps = {
  loading: boolean
  error: string | null
  details: RoomDetailsResponseDTO | null
  refreshRoomDetails: () => Promise<void>
  ownerPlainPassword: string | null
}

export type RoomPageLobbyProps = {
  currentUserReady: boolean | null
  readySaving: boolean
  readyError: string | null
  leaveSaving: boolean
  leaveError: string | null
  onSetReady: (ready: boolean) => void | Promise<void>
  onLeaveRoom: () => void | Promise<void>
}

export type RoomPageGeoProps = {
  geoCenterLat: string
  geoCenterLon: string
  geoMaxDistanceMeters: string
  onGeoCenterLatChange: (value: string) => void
  onGeoCenterLonChange: (value: string) => void
  onGeoMaxDistanceMetersChange: (value: string) => void
  geoSaving: boolean
  geoError: string | null
  onSaveGeoFilter: () => void | Promise<void>
  geoConfirmSaving: boolean
  geoConfirmError: string | null
  onConfirmGeoFilter: () => void | Promise<void>
}

export type RoomPageSessionStartProps = {
  startSaving: boolean
  startError: string | null
  startInfo: string | null
  onStartSession: () => void | Promise<void>
}

export type RoomPageKitchenProps = {
  kitchenCatalog: KitchenTagDTO[] | null
  kitchenCatalogLoading: boolean
  kitchenCatalogError: string | null
  pendingKitchenSlugs: string[]
  setPendingKitchenSlugs: (slugs: string[]) => void
  kitchenAddSaving: boolean
  kitchenRemoveSlug: string | null
  kitchenConfirmSaving: boolean
  kitchenActionError: string | null
  kitchenFiltersLocked: boolean
  currentUserFiltersConfirmed: boolean
  getKitchenTagLabel: (slug: string) => string
  onAddKitchenTags: () => void | Promise<void>
  onRemoveKitchenTag: (slug: string) => void | Promise<void>
  onConfirmKitchenFilters: () => void | Promise<void>
}

export type RoomPageViewProps = {
  summary: RoomPageSummaryProps
  lobby: RoomPageLobbyProps
  geo: RoomPageGeoProps
  sessionStart: RoomPageSessionStartProps
  kitchen: RoomPageKitchenProps
}
