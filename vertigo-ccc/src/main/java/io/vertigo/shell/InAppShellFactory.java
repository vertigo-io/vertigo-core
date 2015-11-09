package io.vertigo.shell;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;

public class InAppShellFactory implements Factory<Command> {

	@Override
	public Command create() {
		return new InAppShell();
	}

}
