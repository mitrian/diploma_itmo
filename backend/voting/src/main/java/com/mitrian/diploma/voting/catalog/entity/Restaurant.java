package com.mitrian.diploma.voting.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "restaurants")
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 512)
	private String name;

	@Column(nullable = false, length = 1024)
	private String address;

	@Column(name = "opening_hours", nullable = false, length = 256)
	private String openingHours;

	@Column(nullable = false, length = 64)
	private String phone;

	@Column(name = "website_url", length = 512)
	private String websiteUrl;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	@Column(name = "kudago_place_id")
	private Long kudagoPlaceId;
}
