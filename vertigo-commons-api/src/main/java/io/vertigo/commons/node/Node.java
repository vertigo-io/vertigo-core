package io.vertigo.commons.node;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.vertigo.lang.Assertion;

/**
 * A Node in a vertigo app.
 * A node has :
 * 		- an id
 * 		- a status
 * 		- the timestamp of the last touch
 * 		- the timestamp of the startup of the app
 * 		- the skills of the node
 * 		- an optional endPoint to reach the node
 * @author mlaroche
 *
 */
public final class Node {

	private final String id;
	private final String appName;

	private final String lastStatus;
	private final Instant lastTouch;
	private final Instant startDate;

	private final List<String> skills;

	private final Optional<String> endPointOpt;

	/**
	 * Constructor.
	 * @param id id of the node (must be unique in an entire app)
	 * @param appName name of the app the node is in
	 * @param lastStatus last status of the node (OK, KO, etc)
	 * @param lastTouch the time of the last info about this node
	 * @param startDate the start date of the node
	 * @param endPointOpt an optional endpoint to reach this node
	 * @param skills the list of capabilities of the node
	 */
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

	/**
	 * The id of the node
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * The name of the app
	 * @return the app name
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * The start date of the node
	 * @return epochMillis
	 */
	public long getStartDate() {
		return startDate.toEpochMilli();
	}

	/**
	 * The optional endPoint of the node
	 * @return the endPoint
	 */
	public Optional<String> getEndPoint() {
		return endPointOpt;
	}

	/**
	 * The protocol of the endpoint (if specified)
	 * @return the protocol
	 */
	public String getProtocol() {
		Assertion.checkState(endPointOpt.isPresent(), "Cannot get a protocol if no Endpoint is defined");
		// ---
		final String endPoint = endPointOpt.get();
		return endPoint.substring(0, endPoint.indexOf(':'));
	}

	/**
	 * The skills of the node (for example editing)
	 * @return the skills of the node
	 */
	public List<String> getSkills() {
		return skills;
	}

	/**
	 * The last status of the node
	 * @return the status of the node
	 */
	public String getLastStatus() {
		return lastStatus;
	}

	/**
	 * The timestamp of the last touch of the node
	 * @return the timestamp of the last touch of the node
	 */
	public Instant getLastTouch() {
		return lastTouch;
	}
}
