package io.vertigo.commons.command;

import io.vertigo.lang.Builder;

public final class CommandResponseBuilder<P> implements Builder<CommandResponse<P>> {

	private CommandResponseStatus myStatus;
	private String myDisplay;
	private String myTargetUrl; // may be null
	private P myPayload; // may be null

	CommandResponseBuilder() {
		//package
	}

	public CommandResponseBuilder<P> withStatus(final CommandResponseStatus status) {
		myStatus = status;
		return this;
	}

	public CommandResponseBuilder<P> withDisplay(final String display) {
		myDisplay = display;
		return this;
	}

	public CommandResponseBuilder<P> withPayload(final P payload) {
		myPayload = payload;
		return this;
	}

	public CommandResponseBuilder<P> withTargetUrl(final String targetUrl) {
		myTargetUrl = targetUrl;
		return this;
	}

	@Override
	public CommandResponse<P> build() {
		return new CommandResponse<>(myStatus, myDisplay, myPayload, myTargetUrl);
	}

}
