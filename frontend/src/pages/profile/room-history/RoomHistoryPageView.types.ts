import type {
  RoomHistoryFiltersDTO,
  RoomHistoryOverviewDTO,
  RoomHistoryParticipantDTO,
  RoomHistoryStageOneSectionDTO,
  RoomHistoryStageOneVoteDTO,
  RoomHistoryStageTwoRowDTO,
  RoomWinnerResponseDTO,
} from '../../../api/types'
import type { RoomHistoryErrorState, RoomHistoryLoadingState } from './RoomHistoryPageState'

export type RoomHistoryPageViewProps = {
  roomCode: string
  overview: RoomHistoryOverviewDTO | null
  participants: RoomHistoryParticipantDTO[] | null
  filters: RoomHistoryFiltersDTO | null
  stageOne: RoomHistoryStageOneSectionDTO | null
  stageTwo: RoomHistoryStageTwoRowDTO[] | null
  winner: RoomWinnerResponseDTO | null
  loading: RoomHistoryLoadingState
  errors: RoomHistoryErrorState
  stageOneVotes: Record<number, RoomHistoryStageOneVoteDTO[]>
  stageOneVotesLoading: Record<number, boolean>
  stageOneVotesError: Record<number, string | null>
  onLoadStageOneVotes: (restaurantId: number) => void | Promise<void>
}
