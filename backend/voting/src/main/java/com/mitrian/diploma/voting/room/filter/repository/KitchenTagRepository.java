package com.mitrian.diploma.voting.room.filter.repository;

import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenTagRepository extends JpaRepository<KitchenTag, Long> {

	Optional<KitchenTag> findBySlug(String slug);

	List<KitchenTag> findBySlugIn(Collection<String> slugs);

	List<KitchenTag> findAllByOrderByIdAsc();
}
