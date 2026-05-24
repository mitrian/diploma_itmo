package com.mitrian.diploma.history.repository;

import com.mitrian.diploma.history.dto.row.FinishedRoomSummaryRow;
import com.mitrian.diploma.history.dto.row.KitchenTagSelectionRow;
import com.mitrian.diploma.history.dto.row.ParticipantRow;
import com.mitrian.diploma.history.dto.row.RestaurantBaseRow;
import com.mitrian.diploma.history.dto.row.RestaurantKitchenTagRow;
import com.mitrian.diploma.history.dto.row.RoomHeaderRow;
import com.mitrian.diploma.history.dto.row.StageOneCandidateRow;
import com.mitrian.diploma.history.dto.row.StageOneFinalistRow;
import com.mitrian.diploma.history.dto.row.StageOneVoteRow;
import com.mitrian.diploma.history.dto.row.StageTwoRankRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class HistoryQueryRepository {

	private final EntityManager em;

	public HistoryQueryRepository(EntityManager em) {
		this.em = em;
	}

	public Optional<RoomHeaderRow> findRoomHeader(String code) {
		String sql = """
			SELECT r.id, r.code, r.state, r.owner_id, u.login, u.display_name,
				r.center_lat, r.center_lon, r.max_distance_meters,
				r.chosen_restaurant_id, r.winner_selection_principle, r.organizer_double_weight_applied,
				r.participant_count, r.stage_one_participant_count_snapshot,
				r.created_at, r.updated_at, r.stage_one_started_at,
				COALESCE(r.finished_at, r.updated_at) AS finished_at
			FROM rooms r
			JOIN users u ON u.id = r.owner_id
			WHERE r.code = :code
			""";
		Query q = em.createNativeQuery(sql).setParameter("code", code);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().findFirst().map(HistoryQueryRepository::mapRoomHeaderRow);
	}

	public boolean existsParticipant(Long roomId, Long userId) {
		String sql = """
			SELECT COUNT(*)
			FROM room_participants rp
			WHERE rp.room_id = :roomId AND rp.user_id = :userId
			""";
		Object single = em.createNativeQuery(sql)
			.setParameter("roomId", roomId)
			.setParameter("userId", userId)
			.getSingleResult();
		long count = toLong(single);
		return count > 0;
	}

	public List<FinishedRoomSummaryRow> findFinishedRoomsForUser(Long userId, String finishedState) {
		String sql = """
			SELECT r.code, r.created_at, COALESCE(r.finished_at, r.updated_at), r.stage_one_started_at,
				r.chosen_restaurant_id, rest.name, r.participant_count, rp.is_owner
			FROM room_participants rp
			JOIN rooms r ON r.id = rp.room_id
			LEFT JOIN restaurants rest ON rest.id = r.chosen_restaurant_id
			WHERE rp.user_id = :userId AND r.state = :finishedState
			ORDER BY COALESCE(r.finished_at, r.updated_at) DESC, r.id DESC
			""";
		Query q = em.createNativeQuery(sql)
			.setParameter("userId", userId)
			.setParameter("finishedState", finishedState);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapFinishedRoomSummaryRow).toList();
	}

	public List<ParticipantRow> findParticipants(Long roomId) {
		String sql = """
			SELECT u.id, u.login, u.display_name, rp.is_owner
			FROM room_participants rp
			JOIN users u ON u.id = rp.user_id
			WHERE rp.room_id = :roomId
			ORDER BY rp.id ASC
			""";
		Query q = em.createNativeQuery(sql).setParameter("roomId", roomId);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapParticipantRow).toList();
	}

	public List<KitchenTagSelectionRow> findKitchenTagSelections(Long roomId) {
		String sql = """
			SELECT u.id, u.login, u.display_name, kt.id, kt.slug, kt.label_ru
			FROM room_kitchen_tag_selections s
			JOIN users u ON u.id = s.user_id
			JOIN kitchen_tags kt ON kt.id = s.kitchen_tag_id
			WHERE s.room_id = :roomId
			ORDER BY kt.id ASC
			""";
		Query q = em.createNativeQuery(sql).setParameter("roomId", roomId);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapKitchenTagSelectionRow).toList();
	}

	public List<StageOneCandidateRow> findStageOneCandidates(Long roomId) {
		String sql = """
			SELECT c.restaurant_id, c.sort_order, r.name, r.address,
				r.opening_hours, r.phone, r.website_url
			FROM room_stage_one_candidates c
			JOIN restaurants r ON r.id = c.restaurant_id
			WHERE c.room_id = :roomId
			ORDER BY c.sort_order ASC
			""";
		Query q = em.createNativeQuery(sql).setParameter("roomId", roomId);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapStageOneCandidateRow).toList();
	}

	public List<StageOneVoteRow> findStageOneVotes(Long roomId) {
		String sql = """
			SELECT u.id, u.login, u.display_name, v.restaurant_id, v.suitable, v.created_at
			FROM room_stage_one_votes v
			JOIN users u ON u.id = v.user_id
			WHERE v.room_id = :roomId
			ORDER BY v.created_at ASC, v.id ASC
			""";
		Query q = em.createNativeQuery(sql).setParameter("roomId", roomId);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapStageOneVoteRow).toList();
	}

	public List<StageOneVoteRow> findStageOneVotesByRestaurant(Long roomId, Long restaurantId) {
		String sql = """
			SELECT u.id, u.login, u.display_name, v.restaurant_id, v.suitable, v.created_at
			FROM room_stage_one_votes v
			JOIN users u ON u.id = v.user_id
			WHERE v.room_id = :roomId AND v.restaurant_id = :restaurantId
			ORDER BY v.created_at ASC, v.id ASC
			""";
		Query q = em.createNativeQuery(sql)
			.setParameter("roomId", roomId)
			.setParameter("restaurantId", restaurantId);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapStageOneVoteRow).toList();
	}

	public List<StageOneFinalistRow> findStageOneFinalists(Long roomId) {
		String sql = """
			SELECT f.restaurant_id, f.approval_count, f.position, f.included_by
			FROM room_stage_one_finalists f
			WHERE f.room_id = :roomId
			ORDER BY f.position ASC
			""";
		Query q = em.createNativeQuery(sql).setParameter("roomId", roomId);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapStageOneFinalistRow).toList();
	}

	public List<StageTwoRankRow> findStageTwoRanks(Long roomId) {
		String sql = """
			SELECT u.id, u.login, u.display_name, k.restaurant_id, k.rank_value
			FROM room_stage_two_ranks k
			JOIN users u ON u.id = k.user_id
			WHERE k.room_id = :roomId
			""";
		Query q = em.createNativeQuery(sql).setParameter("roomId", roomId);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapStageTwoRankRow).toList();
	}

	public List<RestaurantBaseRow> findRestaurantsByIds(Collection<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		String sql = """
			SELECT r.id, r.name, r.address, r.opening_hours, r.phone, r.website_url
			FROM restaurants r
			WHERE r.id IN :ids
			""";
		Query q = em.createNativeQuery(sql).setParameter("ids", ids);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapRestaurantBaseRow).toList();
	}

	public List<RestaurantKitchenTagRow> findRestaurantKitchenTags(Collection<Long> restaurantIds) {
		if (restaurantIds == null || restaurantIds.isEmpty()) {
			return List.of();
		}
		String sql = """
			SELECT rkt.restaurant_id, kt.id, kt.slug, kt.label_ru
			FROM restaurant_kitchen_tags rkt
			JOIN kitchen_tags kt ON kt.id = rkt.kitchen_tag_id
			WHERE rkt.restaurant_id IN :ids
			ORDER BY rkt.restaurant_id ASC, kt.id ASC
			""";
		Query q = em.createNativeQuery(sql).setParameter("ids", restaurantIds);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		return rows.stream().map(HistoryQueryRepository::mapRestaurantKitchenTagRow).toList();
	}

	private static RoomHeaderRow mapRoomHeaderRow(Object[] row) {
		return new RoomHeaderRow(
			toLong(row[0]),
			toString(row[1]),
			toString(row[2]),
			toLong(row[3]),
			toString(row[4]),
			toString(row[5]),
			toDouble(row[6]),
			toDouble(row[7]),
			toInteger(row[8]),
			toLongNullable(row[9]),
			toString(row[10]),
			toBoolean(row[11]),
			toInt(row[12]),
			toInteger(row[13]),
			toLocalDateTime(row[14]),
			toLocalDateTime(row[15]),
			toLocalDateTime(row[16]),
			toLocalDateTime(row[17])
		);
	}

	private static FinishedRoomSummaryRow mapFinishedRoomSummaryRow(Object[] row) {
		return new FinishedRoomSummaryRow(
			toString(row[0]),
			toLocalDateTime(row[1]),
			toLocalDateTime(row[2]),
			toLocalDateTime(row[3]),
			toLongNullable(row[4]),
			toString(row[5]),
			toInt(row[6]),
			toBoolean(row[7])
		);
	}

	private static ParticipantRow mapParticipantRow(Object[] row) {
		return new ParticipantRow(
			toLong(row[0]),
			toString(row[1]),
			toString(row[2]),
			toBoolean(row[3])
		);
	}

	private static KitchenTagSelectionRow mapKitchenTagSelectionRow(Object[] row) {
		return new KitchenTagSelectionRow(
			toLong(row[0]),
			toString(row[1]),
			toString(row[2]),
			toLong(row[3]),
			toString(row[4]),
			toString(row[5])
		);
	}

	private static StageOneCandidateRow mapStageOneCandidateRow(Object[] row) {
		return new StageOneCandidateRow(
			toLong(row[0]),
			toInt(row[1]),
			toString(row[2]),
			toString(row[3]),
			toString(row[4]),
			toString(row[5]),
			toString(row[6])
		);
	}

	private static StageOneVoteRow mapStageOneVoteRow(Object[] row) {
		return new StageOneVoteRow(
			toLong(row[0]),
			toString(row[1]),
			toString(row[2]),
			toLong(row[3]),
			toBoolean(row[4]),
			toLocalDateTime(row[5])
		);
	}

	private static StageOneFinalistRow mapStageOneFinalistRow(Object[] row) {
		return new StageOneFinalistRow(
			toLong(row[0]),
			toInt(row[1]),
			toInt(row[2]),
			toString(row[3])
		);
	}

	private static StageTwoRankRow mapStageTwoRankRow(Object[] row) {
		return new StageTwoRankRow(
			toLong(row[0]),
			toString(row[1]),
			toString(row[2]),
			toLong(row[3]),
			toInt(row[4])
		);
	}

	private static RestaurantBaseRow mapRestaurantBaseRow(Object[] row) {
		return new RestaurantBaseRow(
			toLong(row[0]),
			toString(row[1]),
			toString(row[2]),
			toString(row[3]),
			toString(row[4]),
			toString(row[5])
		);
	}

	private static RestaurantKitchenTagRow mapRestaurantKitchenTagRow(Object[] row) {
		return new RestaurantKitchenTagRow(
			toLong(row[0]),
			toLong(row[1]),
			toString(row[2]),
			toString(row[3])
		);
	}

	private static String toString(Object o) {
		return o == null ? null : String.valueOf(o);
	}

	private static long toLong(Object o) {
		if (o == null) {
			return 0L;
		}
		if (o instanceof Number n) {
			return n.longValue();
		}
		throw new IllegalArgumentException("Expected number, got " + o.getClass());
	}

	private static Long toLongNullable(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Number n) {
			return n.longValue();
		}
		throw new IllegalArgumentException("Expected number, got " + o.getClass());
	}

	private static int toInt(Object o) {
		if (o instanceof Number n) {
			return n.intValue();
		}
		if (o instanceof Boolean b) {
			return b ? 1 : 0;
		}
		return 0;
	}

	private static Integer toInteger(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Integer i) {
			return i;
		}
		if (o instanceof Number n) {
			return n.intValue();
		}
		return null;
	}

	private static Double toDouble(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Double d) {
			return d;
		}
		if (o instanceof Number n) {
			return n.doubleValue();
		}
		return null;
	}

	private static boolean toBoolean(Object o) {
		if (o instanceof Boolean b) {
			return b;
		}
		if (o instanceof Number n) {
			return n.intValue() != 0;
		}
		return false;
	}

	private static LocalDateTime toLocalDateTime(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof LocalDateTime ldt) {
			return ldt;
		}
		if (o instanceof Timestamp ts) {
			return ts.toLocalDateTime();
		}
		if (o instanceof java.util.Date d) {
			return new Timestamp(d.getTime()).toLocalDateTime();
		}
		throw new IllegalArgumentException("Expected temporal, got " + o.getClass());
	}
}
