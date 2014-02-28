package io.vertigo.engines.command.samples;

import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VCommandExecutor;
import io.vertigo.kernel.lang.Assertion;

public final class VPingCommandExecutor implements VCommandExecutor<String> {
	public String exec(VCommand command) {
		Assertion.checkNotNull(command);
		//Assertion.checkArgument(command.getName());
		//---------------------------------------------------------------------
		return "pong";
	}
}
