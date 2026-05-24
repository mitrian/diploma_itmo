package com.mitrian.diploma.voting.stageone.service;

import com.mitrian.diploma.voting.stageone.dto.StageOneVoteSuitableRequestDTO;

public interface StageOneService {

	void vote(String roomCode, String userLogin, long restaurantId, StageOneVoteSuitableRequestDTO request);
}
