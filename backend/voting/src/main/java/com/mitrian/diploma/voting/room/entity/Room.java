package com.mitrian.diploma.voting.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, unique = true, length = 32)
	private String code;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "owner_id", nullable = false)
	private Long ownerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, length = 32)
	private RoomState state;

	@Column(name = "organizer_double_weight_applied", nullable = false)
	private boolean organizerDoubleWeightApplied = false;

	@Column(name = "participant_count", nullable = false)
	private int participantCount = 0;

	@Column(name = "stage_one_participant_count_snapshot")
	private Integer stageOneParticipantCountSnapshot;

	@Column(name = "stage_one_vote_rows_count", nullable = false)
	private int stageOneVoteRowsCount = 0;

	@Column(name = "center_lat")
	private Double centerLat;

	@Column(name = "center_lon")
	private Double centerLon;

	@Column(name = "max_distance_meters")
	private Integer maxDistanceMeters;

	@Column(name = "chosen_restaurant_id")
	private Long chosenRestaurantId;

	@Column(name = "winner_selection_principle", length = 64)
	private String winnerSelectionPrinciple;

	@Column(name = "stage_one_timeout_at")
	private LocalDateTime stageOneTimeoutAt;

	@Column(name = "stage_two_timeout_at")
	private LocalDateTime stageTwoTimeoutAt;

	@Column(name = "stage_one_timeout_processed", nullable = false)
	private boolean stageOneTimeoutProcessed = false;

	@Column(name = "stage_two_timeout_processed", nullable = false)
	private boolean stageTwoTimeoutProcessed = false;

	@Column(name = "stage_one_started_at")
	private LocalDateTime stageOneStartedAt;

	@Column(name = "finished_at")
	private LocalDateTime finishedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
