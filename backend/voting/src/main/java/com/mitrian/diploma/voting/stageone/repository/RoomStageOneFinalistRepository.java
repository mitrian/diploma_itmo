package com.mitrian.diploma.voting.stageone.repository;

import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomStageOneFinalistRepository extends JpaRepository<RoomStageOneFinalist, Long> {

	List<RoomStageOneFinalist> findByRoomIdOrderByPositionAsc(Long roomId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM RoomStageOneFinalist f WHERE f.roomId = :roomId")
	void deleteByRoomId(@Param("roomId") Long roomId);
}
