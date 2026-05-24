package com.mitrian.diploma.voting.stagetwo.repository;

import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomStageTwoRankRepository extends JpaRepository<RoomStageTwoRank, Long> {

	List<RoomStageTwoRank> findByRoomId(Long roomId);

	List<RoomStageTwoRank> findByRoomIdAndUserId(Long roomId, Long userId);

	boolean existsByRoomIdAndUserId(Long roomId, Long userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM RoomStageTwoRank r WHERE r.roomId = :roomId AND r.userId = :userId")
	void deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
