package com.mitrian.diploma.voting.stagetwo.service;

import com.mitrian.diploma.voting.stagetwo.dto.StageTwoStatusResponseDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoSubmitRanksRequestDTO;

public interface StageTwoService {

	StageTwoStatusResponseDTO getStatus(String roomCode, String userLogin);

	void submitRanks(String roomCode, String userLogin, StageTwoSubmitRanksRequestDTO request);

	void finalizeByTimeout(Long roomId);
}
