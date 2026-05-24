package com.mitrian.diploma.voting.stageone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
	name = "room_stage_one_finalists",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"room_id", "restaurant_id"}),
		@UniqueConstraint(columnNames = {"room_id", "position"})
	}
)
public class RoomStageOneFinalist {

	public static final String INCLUDED_BASE_QUORUM = "BASE_QUORUM";
	public static final String INCLUDED_RELAXED_QUORUM = "RELAXED_QUORUM";
	public static final String INCLUDED_ORGANIZER_DOUBLE = "ORGANIZER_DOUBLE";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false)
	private Long roomId;

	@Column(name = "restaurant_id", nullable = false)
	private Long restaurantId;

	@Column(name = "approval_count", nullable = false)
	private Integer approvalCount;

	@Column(name = "included_by", nullable = false, length = 32)
	private String includedBy;

	@Column(nullable = false)
	private Integer position;
}
