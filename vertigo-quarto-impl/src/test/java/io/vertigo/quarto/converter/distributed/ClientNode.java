/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.quarto.converter.distributed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author npiedeloup
 */
final class ClientNode {
	private static final String CP_SEP = System.getProperty("path.separator");
	private static final String CP_DEPENDENCIES_PATH = "./target/dependency/";
	private Process nodeProcess;
	private final List<Thread> subThreads = new ArrayList<>();

	private final int maxLifeTime;

	ClientNode(final int maxLifeTime) {
		this.maxLifeTime = maxLifeTime;
	}

	public void start() throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("java -cp ");
		sb.append("./target/classes");
		sb.append(CP_SEP).append("./target/test-classes");
		for (final File dependencyJar : new File(CP_DEPENDENCIES_PATH).listFiles(new FileFilter() {
			public boolean accept(final File pathname) {
				return pathname.isFile() && pathname.getName().endsWith(".jar");
			}
		})) {
			sb.append(CP_SEP).append(CP_DEPENDENCIES_PATH).append(dependencyJar.getName());
		}
		sb.append(" io.vertigo.quarto.converter.distributed.WorkerNodeStarter " + maxLifeTime);

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
					/*				} finally {
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
