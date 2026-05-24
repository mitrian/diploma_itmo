package com.mitrian.diploma.voting.stageone.repository;

import com.mitrian.diploma.voting.stageone.entity.RoomStageOneVote;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomStageOneVoteRepository extends JpaRepository<RoomStageOneVote, Long> {

	List<RoomStageOneVote> findByRoomIdAndUserId(Long roomId, Long userId);

	boolean existsByRoomIdAndUserId(Long roomId, Long userId);

	boolean existsByRoomIdAndUserIdAndRestaurantId(Long roomId, Long userId, Long restaurantId);

	long countByRoomId(Long roomId);

	List<RoomStageOneVote> findByRoomIdOrderByCreatedAtAscIdAsc(Long roomId);

	@Query(
		value = """
			SELECT created_at FROM room_stage_one_votes
			WHERE room_id = :roomId AND restaurant_id = :restaurantId AND suitable = TRUE
			ORDER BY created_at ASC, id ASC
			OFFSET :offset LIMIT 1
			""",
		nativeQuery = true
	)
	Optional<LocalDateTime> findCreatedAtOfSuitableVoteAtZeroBasedIndex(
		@Param("roomId") Long roomId,
		@Param("restaurantId") Long restaurantId,
		@Param("offset") int offset
	);

	@Query(
		value = """
			SELECT created_at FROM (
				SELECT v.created_at, v.id,
					SUM(CASE WHEN v.user_id = (SELECT owner_id FROM rooms WHERE id = :roomId) THEN 2 ELSE 1 END)
						OVER (ORDER BY v.created_at ASC, v.id ASC) AS cum
				FROM room_stage_one_votes v
				WHERE v.room_id = :roomId AND v.restaurant_id = :restaurantId AND v.suitable = TRUE
			) s
			WHERE s.cum >= :threshold
			ORDER BY s.created_at ASC, s.id ASC
			LIMIT 1
			""",
		nativeQuery = true
	)
	Optional<LocalDateTime> findCreatedAtWhenWeightedSuitableCumulativeFirstReaches(
		@Param("roomId") Long roomId,
		@Param("restaurantId") Long restaurantId,
		@Param("threshold") int threshold
	);
}
