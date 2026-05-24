package com.mitrian.diploma.voting.stagetwo.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.room.util.RoomCodeHelper;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoFinalistRowDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoMyRankRowDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoStatusResponseDTO;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoNotActiveException;
import com.mitrian.diploma.voting.stagetwo.repository.RoomStageTwoRankRepository;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoQueryService;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StageTwoQueryServiceImpl implements StageTwoQueryService {

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final RoomStageOneFinalistRepository roomStageOneFinalistRepository;
	private final RoomStageTwoRankRepository roomStageTwoRankRepository;

	public StageTwoQueryServiceImpl(
		UserRepository userRepository,
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		RoomStageOneFinalistRepository roomStageOneFinalistRepository,
		RoomStageTwoRankRepository roomStageTwoRankRepository
	) {
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.roomStageOneFinalistRepository = roomStageOneFinalistRepository;
		this.roomStageTwoRankRepository = roomStageTwoRankRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public StageTwoStatusResponseDTO getStatus(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoom(roomCode);
		assertParticipant(room.getId(), user.getId());

		if (room.getState() != RoomState.STAGE_TWO && room.getState() != RoomState.FINISHED) {
			throw new StageTwoNotActiveException("Stage two is not available for this room state");
		}

		List<RoomStageOneFinalist> finalistRows =
			roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(room.getId());
		if (room.getState() == RoomState.FINISHED
			&& finalistRows.isEmpty()
			&& room.getChosenRestaurantId() == null) {
			throw new StageTwoNotActiveException("Stage two did not run for this room");
		}

		List<StageTwoFinalistRowDTO> finalists = finalistRows.stream()
			.map(f -> new StageTwoFinalistRowDTO(
				f.getRestaurantId(),
				f.getPosition(),
				f.getApprovalCount(),
				f.getIncludedBy()
			))
			.toList();

		List<StageTwoMyRankRowDTO> myRanks = roomStageTwoRankRepository
			.findByRoomIdAndUserId(room.getId(), user.getId())
			.stream()
			.map(r -> new StageTwoMyRankRowDTO(r.getRestaurantId(), r.getRankValue()))
			.sorted(Comparator.comparingInt(StageTwoMyRankRowDTO::rank))
			.toList();

		return new StageTwoStatusResponseDTO(
			room.getState(),
			room.getChosenRestaurantId(),
			room.getStageTwoTimeoutAt(),
			finalists,
			myRanks
		);
	}

	private Room loadRoom(String roomCode) {
		String normalized = RoomCodeHelper.normalize(roomCode);
		return roomRepository.findByCode(normalized)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
	}

	private void assertParticipant(Long roomId, Long userId) {
		if (!roomParticipantRepository.existsByRoomIdAndUserId(roomId, userId)) {
			throw new NotRoomParticipantException("User is not a participant of this room");
		}
	}
}
