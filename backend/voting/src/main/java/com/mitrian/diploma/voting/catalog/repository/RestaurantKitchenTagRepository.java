package com.mitrian.diploma.voting.catalog.repository;

import com.mitrian.diploma.voting.catalog.entity.RestaurantKitchenTag;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantKitchenTagRepository extends JpaRepository<RestaurantKitchenTag, Long> {

	@Query("SELECT DISTINCT rkt.restaurantId FROM RestaurantKitchenTag rkt WHERE rkt.kitchenTagId IN :tagIds")
	List<Long> findDistinctRestaurantIdsByKitchenTagIdIn(@Param("tagIds") Collection<Long> tagIds);

	List<RestaurantKitchenTag> findByRestaurantIdOrderByKitchenTagIdAsc(Long restaurantId);

	void deleteByRestaurantId(Long restaurantId);
}
