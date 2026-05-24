package com.mitrian.diploma.voting.stageone.service;

import com.mitrian.diploma.voting.stageone.dto.StageOneCurrentResponseDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneStatusResponseDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneUpcomingResponseDTO;

public interface StageOneQueryService {

	StageOneCurrentResponseDTO getCurrent(String roomCode, String userLogin);

	StageOneUpcomingResponseDTO getUpcoming(String roomCode, String userLogin, int limit);

	StageOneStatusResponseDTO getStatus(String roomCode, String userLogin);
}
