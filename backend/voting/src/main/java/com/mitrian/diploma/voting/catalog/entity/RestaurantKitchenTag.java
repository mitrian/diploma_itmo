package com.mitrian.diploma.voting.catalog.entity;

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
	name = "restaurant_kitchen_tags",
	uniqueConstraints = @UniqueConstraint(columnNames = {"restaurant_id", "kitchen_tag_id"})
)
public class RestaurantKitchenTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "restaurant_id", nullable = false)
	private Long restaurantId;

	@Column(name = "kitchen_tag_id", nullable = false)
	private Long kitchenTagId;
}
