package com.mitrian.diploma.voting.catalog.service;

import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import java.util.List;

public interface CandidateSelectionService {

	List<Restaurant> findCandidatesForRoom(Long roomId);
}
