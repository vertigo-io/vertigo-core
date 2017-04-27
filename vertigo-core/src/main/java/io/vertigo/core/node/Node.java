package io.vertigo.core.node;

import java.time.Instant;
import java.util.List;

import io.vertigo.lang.Assertion;

public final class Node {

	private final String id;
	private final String name;

	//	private final String lastStatus;
	//	private final Instant lastTouch;
	private final Instant startDate;

	private final List<String> skills;

	private final String endPoint;

	public Node(
			final String id,
			final String name,
			final String lastStatus,
			final Instant lastTouch,
			final Instant startDate,
			final String endPoint,
			final List<String> skills) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(lastStatus);
		Assertion.checkNotNull(lastTouch);
		Assertion.checkArgNotEmpty(endPoint);
		Assertion.checkNotNull(skills);
		// ---
		this.id = id;
		this.name = name;
		//		this.lastStatus = lastStatus;
		//		this.lastTouch = lastTouch;
		this.startDate = startDate;
		this.endPoint = endPoint;
		this.skills = skills;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getStartDate() {
		return startDate.toEpochMilli();
	}

	public String getEndPoint() {
		return endPoint;
	}

	public String getProtocol() {
		return endPoint.substring(0, endPoint.indexOf(':'));
	}

	public List<String> getSkills() {
		return skills;
	}
}
