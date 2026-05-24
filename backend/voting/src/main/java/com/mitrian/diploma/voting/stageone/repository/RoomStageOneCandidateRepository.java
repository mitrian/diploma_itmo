package com.mitrian.diploma.voting.stageone.repository;

import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomStageOneCandidateRepository extends JpaRepository<RoomStageOneCandidate, Long> {

	List<RoomStageOneCandidate> findByRoomIdOrderBySortOrderAsc(Long roomId);

	Optional<RoomStageOneCandidate> findByRoomIdAndRestaurantId(Long roomId, Long restaurantId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		value = """
			DELETE FROM room_stage_one_candidates c
			WHERE c.room_id = :roomId
			  AND NOT EXISTS (
				SELECT 1 FROM room_stage_one_votes v
				WHERE v.room_id = c.room_id AND v.restaurant_id = c.restaurant_id
			  )
			""",
		nativeQuery = true
	)
	int deleteUnvotedByRoomId(@Param("roomId") Long roomId);
}
