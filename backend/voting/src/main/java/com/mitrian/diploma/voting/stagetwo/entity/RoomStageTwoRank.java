package com.mitrian.diploma.voting.stagetwo.entity;

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
	name = "room_stage_two_ranks",
	uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id", "restaurant_id"})
)
public class RoomStageTwoRank {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false)
	private Long roomId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "restaurant_id", nullable = false)
	private Long restaurantId;

	@Column(name = "rank_value", nullable = false)
	private int rankValue;
}
