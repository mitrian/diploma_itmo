package com.mitrian.diploma.voting.room.repository;

import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import com.mitrian.diploma.voting.room.entity.RoomState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

	boolean existsByRoomIdAndUserId(Long roomId, Long userId);

	Optional<RoomParticipant> findByRoomIdAndUserId(Long roomId, Long userId);

	long countByRoomId(Long roomId);

	List<RoomParticipant> findByRoomIdOrderByIdAsc(Long roomId);

	boolean existsByRoomIdAndReadyIsFalse(Long roomId);

	boolean existsByRoomIdAndFiltersConfirmedFalse(Long roomId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE RoomParticipant rp SET rp.ready = false WHERE rp.roomId = :roomId")
	void clearReadyFlagsForRoom(@Param("roomId") Long roomId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE RoomParticipant rp SET rp.filtersConfirmed = false WHERE rp.roomId = :roomId")
	void clearFiltersConfirmedForRoom(@Param("roomId") Long roomId);

	@Query("""
		SELECT COUNT(rp) FROM RoomParticipant rp, Room r
		WHERE rp.roomId = r.id AND rp.userId = :userId AND r.state <> :finished
		""")
	long countParticipationsInNonFinishedRooms(
		@Param("userId") Long userId,
		@Param("finished") RoomState finished
	);

	@Query("""
		SELECT COUNT(rp) FROM RoomParticipant rp, Room r
		WHERE rp.roomId = r.id AND rp.userId = :userId AND r.state <> :finished AND r.id <> :excludeRoomId
		""")
	long countParticipationsInNonFinishedRoomsExcept(
		@Param("userId") Long userId,
		@Param("finished") RoomState finished,
		@Param("excludeRoomId") Long excludeRoomId
	);

	@Query("""
		SELECT r.code FROM Room r, RoomParticipant rp
		WHERE rp.roomId = r.id AND rp.userId = :userId AND r.state <> :finished
		ORDER BY rp.id ASC
		""")
	List<String> findActiveRoomCodesForUser(
		@Param("userId") Long userId,
		@Param("finished") RoomState finished
	);
}
