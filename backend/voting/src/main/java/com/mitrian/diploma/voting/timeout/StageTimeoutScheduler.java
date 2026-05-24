package com.mitrian.diploma.voting.timeout;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StageTimeoutScheduler {

	private final RoomRepository roomRepository;
	private final StageTimeoutService stageTimeoutService;
	private final int batchSize;

	public StageTimeoutScheduler(
		RoomRepository roomRepository,
		StageTimeoutService stageTimeoutService,
		@Value("${voting.timeout-batch-size:200}") int batchSize
	) {
		this.roomRepository = roomRepository;
		this.stageTimeoutService = stageTimeoutService;
		this.batchSize = Math.max(1, batchSize);
	}

	@Scheduled(fixedDelayString = "${voting.timeout-check-interval-ms:5000}")
	public void processExpiredStages() {
		LocalDateTime now = LocalDateTime.now();
		processExpiredStageOneRooms(now);
		processExpiredStageTwoRooms(now);
	}

	private void processExpiredStageOneRooms(LocalDateTime now) {
		while (true) {
			List<Room> expired = roomRepository
				.findByStateAndStageOneTimeoutProcessedFalseAndStageOneTimeoutAtIsNotNullAndStageOneTimeoutAtLessThanEqualOrderByStageOneTimeoutAtAscIdAsc(
					RoomState.STAGE_ONE,
					now,
					PageRequest.of(0, batchSize)
				);
			if (expired.isEmpty()) {
				return;
			}
			for (Room room : expired) {
				stageTimeoutService.processStageOneTimeout(room.getId(), now);
			}
			if (expired.size() < batchSize) {
				return;
			}
		}
	}

	private void processExpiredStageTwoRooms(LocalDateTime now) {
		while (true) {
			List<Room> expired = roomRepository
				.findByStateAndStageTwoTimeoutProcessedFalseAndStageTwoTimeoutAtIsNotNullAndStageTwoTimeoutAtLessThanEqualOrderByStageTwoTimeoutAtAscIdAsc(
					RoomState.STAGE_TWO,
					now,
					PageRequest.of(0, batchSize)
				);
			if (expired.isEmpty()) {
				return;
			}
			for (Room room : expired) {
				stageTimeoutService.processStageTwoTimeout(room.getId());
			}
			if (expired.size() < batchSize) {
				return;
			}
		}
	}
}
