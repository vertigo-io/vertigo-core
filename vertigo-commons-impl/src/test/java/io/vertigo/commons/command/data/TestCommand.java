package io.vertigo.commons.command.data;

import io.vertigo.commons.command.Command;
import io.vertigo.commons.command.CommandResponse;
import io.vertigo.commons.command.CommandResponseStatus;
import io.vertigo.commons.command.GenericUID;
import io.vertigo.core.component.Component;

public class TestCommand implements Component {

	@Command(handle = "/t/repeat", description = "Repeat anything you say")
	public CommandResponse<String> reply(final String text) {
		return CommandResponse.<String> builder()
				.withStatus(CommandResponseStatus.OK)
				.withDisplay("You typed : " + text)
				.withPayload(text)
				.build();
	}

	@Command(handle = "/t/exists", description = "Test if my object uid exists")
	public CommandResponse<Boolean> reply(final GenericUID<Object> myUid) {
		return CommandResponse.<Boolean> builder()
				.withStatus(CommandResponseStatus.OK)
				.withDisplay(myUid.urn())
				.withPayload(false)
				.build();
	}

}
