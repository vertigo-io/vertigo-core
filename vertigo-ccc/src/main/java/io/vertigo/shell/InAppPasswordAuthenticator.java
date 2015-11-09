package io.vertigo.shell;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class InAppPasswordAuthenticator implements PasswordAuthenticator {
	@Override
	public boolean authenticate(final String username, final String password, final ServerSession session) {
		return username != null && username.equals(password);
	}
}
