package com.mitrian.diploma.voting.room.entity;

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
	name = "room_participants",
	uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"})
)
public class RoomParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false)
	private Long roomId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "is_owner", nullable = false)
	private boolean owner;

	@Column(name = "is_ready", nullable = false)
	private boolean ready;

	@Column(name = "filters_confirmed", nullable = false)
	private boolean filtersConfirmed;
}
