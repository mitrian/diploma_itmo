package com.mitrian.diploma.voting.timeout;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "voting")
public class VotingTimeoutProperties {

	private long stageOneTimeoutSeconds = 720;
	private long stageTwoTimeoutSeconds = 180;
	private long timeoutCheckIntervalMs = 5000;

	public long getStageOneTimeoutSeconds() {
		return stageOneTimeoutSeconds;
	}

	public void setStageOneTimeoutSeconds(long stageOneTimeoutSeconds) {
		this.stageOneTimeoutSeconds = stageOneTimeoutSeconds;
	}

	public long getStageTwoTimeoutSeconds() {
		return stageTwoTimeoutSeconds;
	}

	public void setStageTwoTimeoutSeconds(long stageTwoTimeoutSeconds) {
		this.stageTwoTimeoutSeconds = stageTwoTimeoutSeconds;
	}

	public long getTimeoutCheckIntervalMs() {
		return timeoutCheckIntervalMs;
	}

	public void setTimeoutCheckIntervalMs(long timeoutCheckIntervalMs) {
		this.timeoutCheckIntervalMs = timeoutCheckIntervalMs;
	}
}
