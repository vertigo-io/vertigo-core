package io.vertigo.shell;

import io.vertigo.shell.command.VCommand;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import org.apache.log4j.Logger;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

final class InAppShell implements Command, Runnable {

	private static final Logger log = Logger.getLogger(InAppShell.class);

	public static final boolean IS_MAC_OSX = System.getProperty("os.name").startsWith("Mac OS X");

	private static final String SHELL_THREAD_NAME = "InAppShell";
	private static final String SHELL_PROMPT = "app> ";
	private static final String SHELL_CMD_QUIT = "quit";
	private static final String SHELL_CMD_EXIT = "exit";
	private static final String SHELL_CMD_VERSION = "version";
	private static final String SHELL_CMD_HELP = "help";

	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	private ExitCallback callback;
	//private Environment environment;
	private Thread thread;
	private ConsoleReader reader;
	private PrintWriter writer;

	@Override
	public void setInputStream(final InputStream in) {
		this.in = in;
	}

	@Override
	public void setOutputStream(final OutputStream out) {
		this.out = out;
	}

	@Override
	public void setErrorStream(final OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(final ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(final Environment env) throws IOException {
		//	environment = env;
		thread = new Thread(this, SHELL_THREAD_NAME);
		thread.start();
	}

	@Override
	public void destroy() {
		if (reader != null) {
			reader.shutdown();
		}
		thread.interrupt();
	}

	@Override
	public void run() {
		try {
			reader = new ConsoleReader(in, new FilterOutputStream(out) {
				@Override
				public void write(final int i) throws IOException {
					super.write(i);

					// workaround for MacOSX!! reset line after CR..
					if (IS_MAC_OSX && i == ConsoleReader.CR.toCharArray()[0]) {
						super.write(ConsoleReader.RESET_LINE);
					}
				}
			});
			reader.setPrompt(SHELL_PROMPT);
			reader.addCompleter(new StringsCompleter(SHELL_CMD_QUIT,
					SHELL_CMD_EXIT, SHELL_CMD_VERSION, SHELL_CMD_HELP));
			writer = new PrintWriter(reader.getOutput());

			// output welcome banner on ssh session startup
			writer.println("****************************************************");
			writer.println("*        Welcome to Application Shell.             *");
			writer.println("****************************************************");
			writer.flush();

			String line;
			while ((line = reader.readLine()) != null) {
				handleUserInput(line.trim());
			}
		} catch (final InterruptedIOException e) {
			// Ignore
		} catch (final Exception e) {
			log.error("Error executing InAppShell...", e);
		} finally {
			callback.onExit(0);
		}
	}

	private void handleUserInput(final String line) throws IOException {
		if (line.equalsIgnoreCase(SHELL_CMD_QUIT)
				|| line.equalsIgnoreCase(SHELL_CMD_EXIT)) {
			throw new InterruptedIOException();
		}
		String response;
		new VCommand(line)
		try {
			if (line.equalsIgnoreCase(SHELL_CMD_VERSION)) {
				response = "InApp version 1.0.0";
			} else if (line.equalsIgnoreCase(SHELL_CMD_HELP)) {
				response = "Help is not implemented yet...";
			} else {
				response = "======> \"" + line + "\"";
			}
		} catch (final Exception e) {
			err.write(e.getMessage().getBytes());
			err.flush();
			return;
		}
		writer.println(response);
		writer.flush();
	}
}
