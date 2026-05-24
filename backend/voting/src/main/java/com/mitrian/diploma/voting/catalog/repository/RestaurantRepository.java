package com.mitrian.diploma.voting.catalog.repository;

import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

	List<Restaurant> findAllByIdInOrderByIdAsc(Collection<Long> ids);

	Optional<Restaurant> findByKudagoPlaceId(Long kudagoPlaceId);
}
