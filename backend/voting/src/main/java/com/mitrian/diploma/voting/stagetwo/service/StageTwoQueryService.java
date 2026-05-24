package com.mitrian.diploma.voting.stagetwo.service;

import com.mitrian.diploma.voting.stagetwo.dto.StageTwoStatusResponseDTO;

public interface StageTwoQueryService {

	StageTwoStatusResponseDTO getStatus(String roomCode, String userLogin);
}
