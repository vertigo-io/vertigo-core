package io.vertigo.commons.command;

import io.vertigo.lang.Assertion;

public final class CommandResponse<P> {

	private final CommandResponseStatus responseStatus;
	private final String display;
	private final String targetUrl; // may be null
	private final P payload; // may be null

	CommandResponse(
			final CommandResponseStatus responseStatus,
			final String display,
			final P payload,
			final String targetUrl) {
		Assertion.checkNotNull(responseStatus);
		Assertion.checkNotNull(display);
		//---
		this.responseStatus = responseStatus;
		this.display = display;
		this.payload = payload;
		this.targetUrl = targetUrl;
	}

	public static <P extends Object> CommandResponseBuilder<P> builder() {
		return new CommandResponseBuilder<>();
	}

	public CommandResponseStatus getStatus() {
		return responseStatus;
	}

	public String getDisplay() {
		return display;
	}

	public P getPayload() {
		return payload;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

}
