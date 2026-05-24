package com.mitrian.diploma.voting.room.repository;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

	boolean existsByCode(String code);

	Optional<Room> findByCode(String code);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT r FROM Room r WHERE r.code = :code")
	Optional<Room> findByCodeForUpdate(@Param("code") String code);

	List<Room> findByStateAndStageOneTimeoutProcessedFalseAndStageOneTimeoutAtIsNotNullAndStageOneTimeoutAtLessThanEqualOrderByStageOneTimeoutAtAscIdAsc(
		RoomState state,
		LocalDateTime now,
		Pageable pageable
	);

	List<Room> findByStateAndStageTwoTimeoutProcessedFalseAndStageTwoTimeoutAtIsNotNullAndStageTwoTimeoutAtLessThanEqualOrderByStageTwoTimeoutAtAscIdAsc(
		RoomState state,
		LocalDateTime now,
		Pageable pageable
	);

	List<Room> findByStateIn(List<RoomState> states);
}
