package io.vertigo.shell;

import java.io.IOException;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

public class AppShell {
	public static void main(final String[] args) throws Throwable {
		//	Security.addProvider(new BouncyCastleProvider());
		startShell(5222);
	}

	public static void startShell(final int port) throws IOException {
		final SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		sshd.setPasswordAuthenticator(new InAppPasswordAuthenticator());
		sshd.setShellFactory(new InAppShellFactory());
		sshd.start();
	}
}
