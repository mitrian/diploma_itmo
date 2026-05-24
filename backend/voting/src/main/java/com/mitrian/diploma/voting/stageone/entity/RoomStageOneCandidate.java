package com.mitrian.diploma.voting.stageone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
	name = "room_stage_one_candidates",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"room_id", "sort_order"}),
		@UniqueConstraint(columnNames = {"room_id", "restaurant_id"})
	}
)
public class RoomStageOneCandidate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false)
	private Long roomId;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Column(name = "restaurant_id", nullable = false)
	private Long restaurantId;

	@Column(name = "suitable_count", nullable = false)
	private Integer suitableCount = 0;

	@Column(name = "suitable_weighted_count", nullable = false)
	private Integer suitableWeightedCount = 0;

	@Column(name = "base_reached_at")
	private LocalDateTime baseReachedAt;

	@Column(name = "relaxed_reached_at")
	private LocalDateTime relaxedReachedAt;

	@Column(name = "base_weighted_reached_at")
	private LocalDateTime baseWeightedReachedAt;

	@Column(name = "relaxed_weighted_reached_at")
	private LocalDateTime relaxedWeightedReachedAt;
}
