package io.vertigo.shell;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

public class AppShell {
	public static void main(final String[] args) throws Throwable {

		//	Security.addProvider(new BouncyCastleProvider());

		final SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(5222);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		sshd.setPasswordAuthenticator(new InAppPasswordAuthenticator());
		sshd.setShellFactory(new InAppShellFactory());
		sshd.start();
		System.in.read();
	}
}
