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
import io.vertigo.core.command.VResponse;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * TCP socket Client .
 * @author pchretien
 */
public final class VClient implements AutoCloseable {
	private static int DEFAULT_TIMEOUT = 5000;
	private final SocketChannel socketChannel;
	private final VProtocol protocol = new VProtocol();

	public VClient(final SocketAddress socketAddress) {
		Assertion.checkNotNull(socketAddress);
		//---------------------------------------------------------------------
		try {
			socketChannel = SocketChannel.open(socketAddress);
			socketChannel.socket().setSoTimeout(DEFAULT_TIMEOUT);
			//socketChannel.configureBlocking(true);
			//			socket.setReuseAddress(true);
			//			socket.setKeepAlive(true); //Will monitor the TCP connection is valid
			//			socket.setTcpNoDelay(true); //Socket buffer Whetherclosed, to ensure timely delivery of data
			//			socket.setSoLinger(true, 0); //Control calls close () method, the underlying socket is closed immediately
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			//On notifie l'autre que l'on part.
			//	quit();
			//On ferme tjrs la socket
			socketChannel.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	//
	//	private void quit() {
	//		try {
	//			protocol.fire(socketChannel, "quit");
	//		} catch (IOException e) {
	//			//return VResponse.createResponseWithError(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
	//		}
	//	}

	public VResponse execCommand(final VCommand command) {
		try {
			return protocol.sendCommand(socketChannel, command);
		} catch (final IOException e) {
			return VResponse.createResponseWithError(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
		}
	}

	//	public String getRemoteAddress() {
	//		try {
	//			return socketChannel.getRemoteAddress().toString();
	//		} catch (IOException e) {
	//			throw new RuntimeException(e);
	//		}
	//	}
}
