package com.mitrian.diploma.voting.room.filter.repository;

import com.mitrian.diploma.voting.room.filter.entity.RoomKitchenTagSelection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomKitchenTagSelectionRepository extends JpaRepository<RoomKitchenTagSelection, Long> {

	List<RoomKitchenTagSelection> findByRoomId(Long roomId);

	List<RoomKitchenTagSelection> findByRoomIdAndUserId(Long roomId, Long userId);

	Optional<RoomKitchenTagSelection> findByRoomIdAndUserIdAndKitchenTagId(Long roomId, Long userId, Long kitchenTagId);

	Optional<RoomKitchenTagSelection> findByRoomIdAndKitchenTagId(Long roomId, Long kitchenTagId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM RoomKitchenTagSelection s WHERE s.roomId = :roomId")
	void deleteByRoomId(@Param("roomId") Long roomId);
}
