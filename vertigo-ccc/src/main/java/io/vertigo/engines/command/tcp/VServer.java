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

import io.vertigo.core.command.VCommandHandler;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * A server using non blocking TCP socket .
 * @author pchretien
 */
public final class VServer implements Runnable/*, VEventListener */{
	private static Logger LOG = Logger.getLogger(VServer.class);
	private final int port;
	private final VCommandHandler commandHandler;
	private final VProtocol protocol = new VProtocol();

	/**
	 * Constructor.
	 * @param commandHandler Command handler
	 * @param port tcp port
	 */
	public VServer(final VCommandHandler commandHandler, final int port) {
		Assertion.checkNotNull(commandHandler);
		//-----
		this.commandHandler = commandHandler;
		this.port = port;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try (Selector selector = Selector.open()) {
			try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
				serverSocketChannel.socket().bind(new InetSocketAddress(port));
				serverSocketChannel.configureBlocking(false);

				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				// Wait for events
				while (!Thread.interrupted()) {
					// Wait for an event
					/*int keys =*/selector.select();
					final Iterator<SelectionKey> selectionKeyIt = selector.selectedKeys().iterator();

					while (selectionKeyIt.hasNext()) {
						final SelectionKey selectionKey = selectionKeyIt.next();
						selectionKeyIt.remove();
						//---
						if (!selectionKey.isValid()) {
							continue;
						}
						//---
						if (selectionKey.isAcceptable()) {
							//On accepte une ouverture de socket
							accept(selectionKey);
						}
						if (selectionKey.isReadable()) {
							//On lit sur une socket
							read(selectionKey);
						}
					}
				}
			}
		} catch (final IOException e) {
			LOG.error(e);
		}
	}

	private static void accept(final SelectionKey selectionKey) throws IOException {
		final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

		final SocketChannel socketChannel = serverSocketChannel.accept();
		//System.out.println("$vserver.accept : " + socketChannel.getRemoteAddress());
		socketChannel.configureBlocking(false);

		socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
	}

	//Read a command and execute.
	private void read(final SelectionKey selectionKey) throws IOException {
		//System.out.println("$vserver.read");
		final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		protocol.execCommand(socketChannel, commandHandler);
		//System.out.println("$vserver.read : ok");
	}
}
