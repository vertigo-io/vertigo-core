package io.vertigo.dynamo.work.distributed.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author npiedeloup
 * $Id: ClientNode.java,v 1.5 2014/02/27 10:25:21 pchretien Exp $
 */
final class ClientNode {
	private Process nodeProcess;
	private final List<Thread> subThreads = new ArrayList<>();
	private final int maxLifeTime;

	/**
	 * Constructeur.
	 * @param maxLifeTime Dur�e de vie max en seconde
	 */
	ClientNode(final int maxLifeTime) {
		this.maxLifeTime = maxLifeTime;
	}

	public void start() throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("java -cp ");
		sb.append(System.getProperty("java.class.path"));
		sb.append(" io.vertigo.dynamo.work.distributed.rest.WorkerNodeStarter " + maxLifeTime);

		nodeProcess = Runtime.getRuntime().exec(sb.toString());
		subThreads.add(createMaxLifeTime());
		subThreads.add(createOutputFlusher(nodeProcess.getInputStream(), "[ClientNode] ", System.out));
		subThreads.add(createOutputFlusher(nodeProcess.getErrorStream(), "[ClientNode-err] ", System.err));
		for (final Thread subThread : subThreads) {
			subThread.setDaemon(true);
			subThread.start();
		}
	}

	private Thread createMaxLifeTime() {
		return new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(maxLifeTime * 1000);
					stop();
				} catch (final InterruptedException e) {
					//rien
				}
			}
		});
	}

	public void stop() throws InterruptedException {
		nodeProcess.destroy();
		Thread.sleep(250);
		for (final Thread subThread : subThreads) {
			subThread.interrupt();
		}
	}

	private static Thread createOutputFlusher(final InputStream inputStream, final String prefix, final PrintStream out) {
		return new Thread(new Runnable() {
			public void run() {
				try (final InputStreamReader isr = new InputStreamReader(inputStream)) {
					try (final BufferedReader br = new BufferedReader(isr)) {
						String line;
						out.println(prefix + " Start outputFlusher");
						while (!Thread.interrupted()) {
							while ((line = br.readLine()) != null) {
								out.println(prefix + line);
							}
							Thread.sleep(250);
						}
					}
				} catch (final InterruptedException | IOException e) {
					return;
				}
			}
		}, "ClientNodeOutputFlusher");
	}
}
