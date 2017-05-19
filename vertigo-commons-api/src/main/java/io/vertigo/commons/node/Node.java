package io.vertigo.commons.node;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.vertigo.lang.Assertion;

public final class Node {

	private final String id;
	private final String appName;

	private final String lastStatus;
	private final Instant lastTouch;
	private final Instant startDate;

	private final List<String> skills;

	private final Optional<String> endPointOpt;

	public Node(
			final String id,
			final String appName,
			final String lastStatus,
			final Instant lastTouch,
			final Instant startDate,
			final Optional<String> endPointOpt,
			final List<String> skills) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkArgNotEmpty(appName);
		Assertion.checkNotNull(lastStatus);
		Assertion.checkNotNull(lastTouch);
		Assertion.checkNotNull(endPointOpt);
		Assertion.checkNotNull(skills);
		// ---
		this.id = id;
		this.appName = appName;
		this.lastStatus = lastStatus;
		this.lastTouch = lastTouch;
		this.startDate = startDate;
		this.endPointOpt = endPointOpt;
		this.skills = skills;
	}

	public String getId() {
		return id;
	}

	public String getAppName() {
		return appName;
	}

	public long getStartDate() {
		return startDate.toEpochMilli();
	}

	public Optional<String> getEndPoint() {
		return endPointOpt;
	}

	public String getProtocol() {
		Assertion.checkState(endPointOpt.isPresent(), "Cannot get a protocol if no Endpoint is defined");
		// ---
		final String endPoint = endPointOpt.get();
		return endPoint.substring(0, endPoint.indexOf(':'));
	}

	public List<String> getSkills() {
		return skills;
	}

	public String getLastStatus() {
		return lastStatus;
	}

	public Instant getLastTouch() {
		return lastTouch;
	}
}
