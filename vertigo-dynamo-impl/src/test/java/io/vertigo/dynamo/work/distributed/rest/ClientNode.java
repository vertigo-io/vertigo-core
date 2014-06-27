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
	private static final String CP_SEP = System.getProperty("path.separator");
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
		//sb.append("./kasper-demo-uistruts/src/main/webapp/WEB-INF/classes");
		sb.append("./kasper-commons/target/classes/");
		sb.append(CP_SEP).append("./kasper-commons/target/test-classes/");
		sb.append(CP_SEP).append("./kasper-external/lib/vertigo-starter-0.2.0-SNAPSHOT.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/javax.inject-1.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/log4j-1.2.16.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/jersey-client-1.17.1.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/jersey-server-1.17.1.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/jersey-grizzly2-1.17.1.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/jersey-core-1.17.1.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/gson-2.2.4.jar");
		sb.append(CP_SEP).append("./kasper-external/lib/cglib-2.2.2.jar");
		sb.append(" kasper.work.distributed.rest.WorkerNodeStarter " + maxLifeTime);

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

	private Thread createOutputFlusher(final InputStream inputStream, final String prefix, final PrintStream out) {
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
				} catch (final IOException e) {
					return;
				} catch (final InterruptedException e) {
					return;
					/*} finally {
								try {
									isr.close();
								} catch (final IOException e) {
									//rien
								}
								out.println(prefix + " Terminate outputFlusher");*/
				}
			}
		}, "ClientNodeOutputFlusher");
	}

}
