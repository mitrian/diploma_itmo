package com.mitrian.diploma.history.mapper;

import com.mitrian.diploma.history.dto.HistoryRoomState;
import com.mitrian.diploma.history.dto.RoomHistoryFiltersDTO;
import com.mitrian.diploma.history.dto.RoomHistoryKitchenTagDTO;
import com.mitrian.diploma.history.dto.RoomHistoryOverviewDTO;
import com.mitrian.diploma.history.dto.RoomHistoryParticipantDTO;
import com.mitrian.diploma.history.dto.RoomHistoryRestaurantKitchenTagDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneOutcomeDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneRowDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneSectionDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneVoteDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageTwoRankDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageTwoRowDTO;
import com.mitrian.diploma.history.dto.RoomHistorySummaryDTO;
import com.mitrian.diploma.history.dto.WinnerSelectionPrinciple;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class HistoryMapper {

	public RoomHistorySummaryDTO toSummary(FinishedRoomSummaryRow row) {
		return new RoomHistorySummaryDTO(
			row.roomCode(),
			row.createdAt(),
			row.finishedAt(),
			votingDurationSeconds(row.stageOneStartedAt(), row.finishedAt()),
			row.chosenRestaurantId(),
			row.winnerRestaurantName(),
			row.participantCount(),
			row.viewerWasOwner()
		);
	}

	public RoomHistoryOverviewDTO toOverview(RoomHeaderRow header, String winnerRestaurantName) {
		return new RoomHistoryOverviewDTO(
			header.code(),
			HistoryRoomState.fromPersisted(header.state()),
			header.createdAt(),
			header.finishedAt(),
			header.stageOneStartedAt(),
			votingDurationSeconds(header.stageOneStartedAt(), header.finishedAt()),
			header.ownerDisplayName(),
			header.participantCount(),
			header.chosenRestaurantId(),
			winnerRestaurantName,
			parseStoredWinnerPrinciple(header.winnerSelectionPrinciple()),
			header.organizerDoubleWeightApplied()
		);
	}

	private static Long votingDurationSeconds(LocalDateTime startedAt, LocalDateTime finishedAt) {
		if (startedAt == null || finishedAt == null || finishedAt.isBefore(startedAt)) {
			return null;
		}
		return Duration.between(startedAt, finishedAt).getSeconds();
	}

	private static WinnerSelectionPrinciple parseStoredWinnerPrinciple(String stored) {
		if (stored == null || stored.isBlank()) {
			return WinnerSelectionPrinciple.NONE;
		}
		try {
			return WinnerSelectionPrinciple.valueOf(stored.trim());
		} catch (IllegalArgumentException ex) {
			return WinnerSelectionPrinciple.NONE;
		}
	}

	public List<RoomHistoryParticipantDTO> toParticipants(List<ParticipantRow> rows) {
		return rows.stream()
			.map(p -> new RoomHistoryParticipantDTO(p.displayName(), p.owner()))
			.toList();
	}

	public RoomHistoryFiltersDTO toFilters(RoomHeaderRow header, List<KitchenTagSelectionRow> selections) {
		List<RoomHistoryKitchenTagDTO> kitchenTags = selections.stream()
			.map(s -> new RoomHistoryKitchenTagDTO(
				s.slug(), s.labelRu(), s.userLogin(), s.userDisplayName()
			))
			.toList();
		return new RoomHistoryFiltersDTO(
			header.centerLat(),
			header.centerLon(),
			header.maxDistanceMeters(),
			kitchenTags
		);
	}

	public Map<Long, List<RoomHistoryRestaurantKitchenTagDTO>> mapRestaurantKitchenTagsByRestaurant(
		List<RestaurantKitchenTagRow> rows
	) {
		Map<Long, List<RoomHistoryRestaurantKitchenTagDTO>> result = new HashMap<>();
		for (RestaurantKitchenTagRow row : rows) {
			result.computeIfAbsent(row.restaurantId(), k -> new ArrayList<>())
				.add(new RoomHistoryRestaurantKitchenTagDTO(
					row.kitchenTagId(),
					row.slug(),
					row.labelRu()
				));
		}
		return result;
	}

	public RoomHistoryStageOneSectionDTO toStageOneSection(
		RoomHeaderRow header,
		List<StageOneCandidateRow> candidates,
		List<StageOneVoteRow> votes,
		List<StageOneFinalistRow> finalists,
		Map<Long, List<RoomHistoryRestaurantKitchenTagDTO>> kitchenTagsByRestaurant
	) {
		Map<Long, List<StageOneVoteRow>> votesByRestaurant = votes.stream()
			.collect(Collectors.groupingBy(StageOneVoteRow::restaurantId));
		Map<Long, String> includedByByRestaurant = finalists.stream()
			.collect(Collectors.toMap(StageOneFinalistRow::restaurantId, StageOneFinalistRow::includedBy));

		List<RoomHistoryStageOneRowDTO> rows = new ArrayList<>(candidates.size());
		for (StageOneCandidateRow c : candidates) {
			List<StageOneVoteRow> rVotes = votesByRestaurant.getOrDefault(c.restaurantId(), List.of());
			int suitable = (int) rVotes.stream().filter(StageOneVoteRow::suitable).count();
			int unsuitable = rVotes.size() - suitable;
			rows.add(new RoomHistoryStageOneRowDTO(
				c.restaurantId(),
				c.restaurantName(),
				c.restaurantAddress(),
				c.openingHours(),
				c.phone(),
				c.websiteUrl(),
				kitchenTagsByRestaurant.getOrDefault(c.restaurantId(), List.of()),
				c.sortOrder(),
				suitable,
				unsuitable,
				includedByByRestaurant.get(c.restaurantId())
			));
		}

		int participantCount = header.stageOneParticipantCountSnapshot() != null
			? header.stageOneParticipantCountSnapshot()
			: header.participantCount();
		int baseQuorum = participantCount > 0 ? Math.floorDiv(participantCount, 2) + 1 : 0;
		int relaxedQuorum = participantCount > 0 ? Math.floorDiv(participantCount, 2) : 0;

		return new RoomHistoryStageOneSectionDTO(
			new RoomHistoryStageOneOutcomeDTO(
				participantCount,
				baseQuorum,
				relaxedQuorum,
				header.organizerDoubleWeightApplied()
			),
			rows
		);
	}

	public List<RoomHistoryStageOneVoteDTO> toStageOneRestaurantVotes(List<StageOneVoteRow> votes) {
		return votes.stream()
			.map(v -> new RoomHistoryStageOneVoteDTO(v.userDisplayName(), v.suitable()))
			.toList();
	}

	public List<RoomHistoryStageTwoRowDTO> toStageTwoRows(
		RoomHeaderRow header,
		List<StageOneFinalistRow> finalists,
		List<StageTwoRankRow> ranks,
		Map<Long, RestaurantBaseRow> finalistCardsById,
		Map<Long, List<RoomHistoryRestaurantKitchenTagDTO>> kitchenTagsByRestaurant
	) {
		Map<Long, List<StageTwoRankRow>> ranksByRestaurant = ranks.stream()
			.collect(Collectors.groupingBy(StageTwoRankRow::restaurantId));

		List<RoomHistoryStageTwoRowDTO> rows = new ArrayList<>(finalists.size());
		for (StageOneFinalistRow f : finalists) {
			RestaurantBaseRow card = finalistCardsById.get(f.restaurantId());
			List<StageTwoRankRow> rRanks = ranksByRestaurant.getOrDefault(f.restaurantId(), List.of());
			int rankSum = rRanks.stream().mapToInt(StageTwoRankRow::rankValue).sum();
			List<RoomHistoryStageTwoRankDTO> rankDtos = rRanks.stream()
				.sorted(Comparator.comparingInt(StageTwoRankRow::rankValue)
					.thenComparing(StageTwoRankRow::userLogin))
				.map(r -> new RoomHistoryStageTwoRankDTO(r.userLogin(), r.userDisplayName(), r.rankValue()))
				.toList();
			rows.add(new RoomHistoryStageTwoRowDTO(
				f.restaurantId(),
				card != null ? card.name() : null,
				card != null ? card.address() : null,
				card != null ? card.openingHours() : null,
				card != null ? card.phone() : null,
				card != null ? card.websiteUrl() : null,
				kitchenTagsByRestaurant.getOrDefault(f.restaurantId(), List.of()),
				f.position(),
				f.approvalCount(),
				f.includedBy(),
				rankSum,
				rankDtos
			));
		}
		return rows;
	}
}
