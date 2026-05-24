export type RoomHistoryLoadingState = {
  overview: boolean
  participants: boolean
  filters: boolean
  stageOne: boolean
  stageTwo: boolean
  winner: boolean
}

export type RoomHistoryErrorState = {
  overview: string | null
  participants: string | null
  filters: string | null
  stageOne: string | null
  stageTwo: string | null
  winner: string | null
}
