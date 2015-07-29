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
package io.vertigo.engines.command.tcp;

import io.vertigo.core.command.VCommand;
import io.vertigo.core.command.VCommandHandler;
import io.vertigo.core.command.VResponse;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * This test start
 * - 1 TCP server
 * - n TCP clients (each clientis a separated java thread)
 *
 *  a test sequence is a loop.
 * @author pchretien
 */
public final class TcpTest {
	private static final String HOST = "localhost";
	//volontairement différent de 4444
	private static final int PORT = 4443;

	@Test
	public void testRequestServer() throws InterruptedException {
		startServer();
		test(2, 10000);
		test(2, 10000);
		test(2, 10000);
		test(2, 10000);
		//test(2, 3);
	}

	private static void startServer() {
		new Thread(new VServer(new MyCommandHandler(), PORT)).start();
	}

	public void test(final int threadCount, final int count) throws InterruptedException {
		final SharedExecStatus sharedExecStatus = new SharedExecStatus();

		final Thread[] threads = new Thread[threadCount];
		final long start = System.currentTimeMillis();
		for (int j = 0; j < threadCount; j++) {
			threads[j] = new Thread(new Sender(count, sharedExecStatus));
			threads[j].start();
		}
		for (int j = 0; j < threadCount; j++) {
			threads[j].join();
		}
		System.out.println("--------------------------------------------------- ");
		System.out.println("----- threads       : " + threadCount);
		System.out.println("----- count/thread  : " + count);
		System.out.println("----- elapsed time  : " + ((System.currentTimeMillis() - start) / 1000) + "s");
		System.out.println("--------------------------------------------------- ");
		Assert.assertFalse(sharedExecStatus.hasErrors());
	}

	public static final class SharedExecStatus {
		//private final int id;
		private final List<String> errors = new ArrayList<>();

		public synchronized void addError(final String error) {
			errors.add(error);
		}

		public synchronized boolean hasErrors() {
			return !errors.isEmpty();
		}

		public synchronized void logErrors() {
			for (final String error : errors) {
				System.err.println(error);
			}
		}
	}

	public static final class Sender implements Runnable {
		//private final int id;
		private final int count;
		private final SharedExecStatus sharedExecStatus;

		Sender(final int count, final SharedExecStatus sharedExecStatus) {
			this.count = count;
			this.sharedExecStatus = sharedExecStatus;
		}

		static VClient createClient() {
			final SocketAddress socketAddress = new InetSocketAddress(HOST, PORT);
			return new VClient(socketAddress);
		}

		@Override
		public void run() {
			try (VClient tcpClient = createClient()) {
				for (int i = 0; i < count; i++) {
					//if (i % 10 == 0) {
					//	System.out.println(">>[" + id + "] :" + i);
					//}
					final VResponse response = tcpClient.execCommand(new VCommand("ping"));
					if (response.hasError()) {
						sharedExecStatus.addError("response.hasError");
					}
					if (!response.getResponse().contains("pong")) {
						sharedExecStatus.addError("!response.contains(pong)");
					}
				}
			}
		}
	}

	private static class MyCommandHandler implements VCommandHandler {
		@Override
		public VResponse onCommand(final VCommand command) {
			if ("ping".equals(command.getName())) {
				return VResponse.createResponse("pong");
			} else if ("pong".equals(command.getName())) {
				return VResponse.createResponse("ping");
			}
			return VResponse.createResponseWithError("unknown command " + command.getName());
		}
	}
}
