import { Link } from 'react-router-dom'
import type { RoomHistoryPageViewProps } from './RoomHistoryPageView.types'
import { RoomInfoSection } from './RoomInfoSection'
import { StageOneSection } from './StageOneSection'
import { StageTwoSection } from './StageTwoSection'
import { WinnerSection } from './WinnerSection'
import '../../../styles/ProfilePage.css'

export type { RoomHistoryPageViewProps } from './RoomHistoryPageView.types'

export function RoomHistoryPageView(props: RoomHistoryPageViewProps) {
  const {
    roomCode,
    overview,
    participants,
    filters,
    stageOne,
    stageTwo,
    winner,
    loading,
    errors,
    stageOneVotes,
    stageOneVotesLoading,
    stageOneVotesError,
    onLoadStageOneVotes,
  } = props

  return (
    <div className="profile-room">
      <Link to="/profile" className="profile-room__back">
        ← К списку сессий
      </Link>
      <h1 className="profile-room__title">История сессии {roomCode}</h1>

      <WinnerSection
        overview={overview}
        winner={winner}
        loading={loading.overview || loading.winner}
        error={errors.overview ?? errors.winner}
      />

      <RoomInfoSection
        overview={overview}
        participants={participants}
        filters={filters}
        loading={{
          overview: loading.overview,
          participants: loading.participants,
          filters: loading.filters,
        }}
        error={errors.overview ?? errors.participants ?? errors.filters}
      />

      <StageOneSection
        section={stageOne}
        loading={loading.stageOne}
        error={errors.stageOne}
        stageOneVotes={stageOneVotes}
        stageOneVotesLoading={stageOneVotesLoading}
        stageOneVotesError={stageOneVotesError}
        onLoadVotes={onLoadStageOneVotes}
      />

      <StageTwoSection
        rows={stageTwo}
        loading={loading.stageTwo}
        error={errors.stageTwo}
        winnerId={overview?.chosenRestaurantId ?? null}
        winnerPrinciple={overview?.winnerPrinciple ?? 'NONE'}
      />
    </div>
  )
}
