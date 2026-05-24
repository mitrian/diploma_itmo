export type AuthResponseDTO = {
  token: string
  displayName: string
}

export type UserMeResponseDTO = {
  id: number
  login: string
  displayName: string
}

export type RoomState = 'LOBBY' | 'GEO_FILTERS' | 'AWAITING_START' | 'STAGE_ONE' | 'STAGE_TWO' | 'FINISHED'

export type CreateRoomResponseDTO = {
  id: number
  code: string
  state: RoomState
  createdAt: string
}

export type JoinRoomResponseDTO = {
  roomId: number
  code: string
  state: RoomState
}

export type RoomParticipantViewDTO = {
  displayName: string
  owner: boolean
  ready: boolean
  filtersConfirmed: boolean
}

export type KitchenTagDTO = {
  id: number
  slug: string
  labelRu: string
}

export type RoomDetailsResponseDTO = {
  code: string
  state: RoomState
  centerLat: number | null
  centerLon: number | null
  maxDistanceMeters: number | null
  currentUserIsOwner: boolean
  participants: RoomParticipantViewDTO[]
  roomKitchenTags: KitchenTagDTO[]
  myKitchenTagSlugs: string[]
}

export type ActiveRoomResponseDTO = {
  code: string | null
}

export type RestaurantCardDTO = {
  id: number
  name: string
  address: string
  openingHours: string
  phone: string
  websiteUrl: string | null
  kitchenTags: KitchenTagDTO[]
}

export type StageOneUpcomingResponseDTO = {
  completed: boolean
  cards: RestaurantCardDTO[]
}

export type StageOneFinalistRowDTO = {
  position: number
  restaurantId: number
  card: RestaurantCardDTO
  approvalCount: number
  includedBy: string
}

export type StageOneStatusResponseDTO = {
  roomState: RoomState
  stageOneActive: boolean
  participantCount: number
  baseQuorum: number
  relaxedQuorum: number
  timeoutAt: string | null
  finalists: StageOneFinalistRowDTO[]
}

export type StageTwoFinalistRowDTO = {
  restaurantId: number
  position: number
  approvalCount: number
  includedBy: string
}

export type StageTwoMyRankRowDTO = {
  restaurantId: number
  rank: number
}

export type StageTwoStatusResponseDTO = {
  roomState: RoomState
  chosenRestaurantId: number | null
  timeoutAt: string | null
  finalists: StageTwoFinalistRowDTO[]
  myRanks: StageTwoMyRankRowDTO[]
}

export type StageTwoRankEntryDTO = {
  restaurantId: number
  rank: number
}

export type RoomWinnerResponseDTO = {
  roomState: RoomState
  chosenRestaurantId: number | null
  winnerRestaurant: RestaurantCardDTO | null
}

export type WinnerSelectionPrinciple =
  | 'NONE'
  | 'STAGE_ONE_BASE_QUORUM_SINGLE'
  | 'STAGE_TWO_RANK_SUM_UNIQUE_LEADER'
  | 'STAGE_TWO_TIEBREAK_BY_APPROVAL_COUNT'
  | 'STAGE_TWO_TIEBREAK_BY_ORGANIZER_RANK'
  | 'STAGE_TWO_TIEBREAK_BY_STAGE_ONE_POSITION'

const WINNER_SELECTION_PRINCIPLE_VALUES: readonly WinnerSelectionPrinciple[] = [
  'NONE',
  'STAGE_ONE_BASE_QUORUM_SINGLE',
  'STAGE_TWO_RANK_SUM_UNIQUE_LEADER',
  'STAGE_TWO_TIEBREAK_BY_APPROVAL_COUNT',
  'STAGE_TWO_TIEBREAK_BY_ORGANIZER_RANK',
  'STAGE_TWO_TIEBREAK_BY_STAGE_ONE_POSITION',
]

const WINNER_SELECTION_PRINCIPLE_SET = new Set<string>(WINNER_SELECTION_PRINCIPLE_VALUES)

/** Значение приходит с бэкенда из колонки `rooms.winner_selection_principle` (фиксируется при FINISHED). */
export function isWinnerSelectionPrinciple(value: string): value is WinnerSelectionPrinciple {
  return WINNER_SELECTION_PRINCIPLE_SET.has(value)
}

export type RoomHistorySummaryDTO = {
  roomCode: string
  createdAt: string
  finishedAt: string
  votingDurationSeconds: number | null
  chosenRestaurantId: number | null
  winnerRestaurantName: string | null
  participantCount: number
  viewerWasOwner: boolean
}

export type RoomHistoryOverviewDTO = {
  roomCode: string
  roomState: RoomState
  createdAt: string
  finishedAt: string
  stageOneStartedAt: string | null
  votingDurationSeconds: number | null
  ownerDisplayName: string
  participantCount: number
  chosenRestaurantId: number | null
  winnerRestaurantName: string | null
  winnerPrinciple: WinnerSelectionPrinciple
  organizerDoubleWeightApplied: boolean
}

export type RoomHistoryParticipantDTO = {
  displayName: string
  owner: boolean
}

export type RoomHistoryKitchenTagDTO = {
  slug: string
  labelRu: string
  pickedByLogin: string
  pickedByDisplayName: string
}

export type RoomHistoryFiltersDTO = {
  centerLat: number | null
  centerLon: number | null
  maxDistanceMeters: number | null
  kitchenTags: RoomHistoryKitchenTagDTO[]
}

export type RoomHistoryStageOneOutcomeDTO = {
  participantCount: number
  baseQuorum: number
  relaxedQuorum: number
  organizerDoubleWeightApplied: boolean
}

export type RoomHistoryStageOneRowDTO = {
  restaurantId: number
  name: string
  address: string
  openingHours: string
  phone: string
  websiteUrl: string | null
  kitchenTags: KitchenTagDTO[]
  sortOrder: number
  totalSuitable: number
  totalUnsuitable: number
  includedBy: string | null
}

export type RoomHistoryStageOneSectionDTO = {
  outcome: RoomHistoryStageOneOutcomeDTO
  restaurants: RoomHistoryStageOneRowDTO[]
}

export type RoomHistoryStageOneVoteDTO = {
  userDisplayName: string
  suitable: boolean
}

export type RoomHistoryStageTwoRankDTO = {
  userLogin: string
  userDisplayName: string
  rank: number
}

export type RoomHistoryStageTwoRowDTO = {
  restaurantId: number
  name: string | null
  address: string | null
  openingHours: string | null
  phone: string | null
  websiteUrl: string | null
  kitchenTags: KitchenTagDTO[]
  stageOnePosition: number
  stageOneApprovalCount: number
  includedBy: string | null
  rankSum: number
  ranks: RoomHistoryStageTwoRankDTO[]
}
