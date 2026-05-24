package com.mitrian.diploma.voting.room.filter.entity;

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
	name = "room_kitchen_tag_selections",
	uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "kitchen_tag_id"})
)
public class RoomKitchenTagSelection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false)
	private Long roomId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "kitchen_tag_id", nullable = false)
	private Long kitchenTagId;
}
