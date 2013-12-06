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
package io.vertigoimpl.engines.command.tcp;

import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VCommandHandler;
import io.vertigo.kernel.command.VResponse;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author pchretien
 */
final class VProtocol {
	static final String CHARSET = "UTF-8";

	private final ByteBuffer buffer;

	//private final VEventListener eventListener;

	VProtocol(/*VEventListener eventListener*/) {
		//	Assertion.checkNotNull(eventListener);
		//---------------------------------------------------------------------
		//Todo � optimiser
		buffer = ByteBuffer.allocate(1024 * 1024);
		//		this.eventListener = eventListener;
	}

	//primitive : Protocol agnostic
	private void push(final SocketChannel socketChannel, String data) throws IOException {
		Assertion.checkNotNull(socketChannel);
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		//System.out.println("  $push : " + socketChannel.getLocalAddress() + " >>>> " + socketChannel.getRemoteAddress() + " size=" + data.length());
		//---

		//ByteBuffer wrappedBuffer = ByteBuffer.wrap(data.getBytes(CHARSET));
		buffer.clear();
		buffer.put(data.getBytes(CHARSET));

		buffer.flip();
		//Et on �crit les donn�es
		while (buffer.hasRemaining()) {
			socketChannel.write(buffer);
		}
	}

	//primitive : Protocol agnostic
	//return null si le flux est fini
	private String pull(final SocketChannel socketChannel) throws IOException {
		Assertion.checkNotNull(socketChannel);
		//---------------------------------------------------------------------
		//System.out.println("  $pull : " + socketChannel.getLocalAddress() + "<<<< " + socketChannel.getRemoteAddress());
		//---
		buffer.clear();
		int bytesRead = socketChannel.read(buffer);

		if (bytesRead == -1) {
			//			System.out.println("  socketChannel :" + socketChannel.getRemoteAddress() + " - connected           :" + socketChannel.isConnected());
			//			System.out.println("  socketChannel :" + socketChannel.getRemoteAddress() + " - isBlocking          :" + socketChannel.isBlocking());
			//			System.out.println("  socketChannel :" + socketChannel.getRemoteAddress() + " - isRegistered        :" + socketChannel.isRegistered());
			//			System.out.println("  close socketChannel :" + socketChannel.getRemoteAddress());

			// No more bytes can be read from the channel
			socketChannel.close();
			return null;
		}

		// To read the bytes, flip the buffer
		buffer.flip();

		final StringBuilder sb = new StringBuilder();
		while (buffer.hasRemaining()) {
			sb.append((char) buffer.get());
		}
		return sb.toString();
	}

	//=========================================================================
	//=============================Fire =======================================
	//=========================================================================
	//	//client side
	//	public void fire(final SocketChannel socketChannel, String notification) throws IOException {
	//		push(socketChannel, "!" + notification);
	//	}

	//=========================================================================
	//=============================Command / Response =========================
	//=========================================================================
	//client side
	VResponse sendCommand(final SocketChannel socketChannel, VCommand command) throws IOException {
		Assertion.checkNotNull(socketChannel);
		Assertion.checkNotNull(command);
		//---------------------------------------------------------------------
		//System.out.println("$sendCommand : " + command);
		//On envoit la request et on attend la response.
		//TODO Envoyer les map
		push(socketChannel, "$" + command.getName());

		//
		String response = pull(socketChannel);
		if (response == null) {
			return VResponse.createResponseWithError("no data received from server");
		} else if (response.startsWith("+")) {
			//	System.out.println("$sendCommand : " + command + " / OK");
			return VResponse.createResponse(response.substring(1));
		} else if (response.startsWith("-")) {
			//	System.out.println("$sendCommand : " + command + " / KO");
			return VResponse.createResponseWithError(response.substring(1));
		}
		throw new VRuntimeException("malformed protocol");
	}

	//server side
	void execCommand(final SocketChannel socketChannel, VCommandHandler requestHandler) throws IOException {
		//1. Routage 
		//System.out.println("$execCommand 1 [waiting]");
		final String dataReceived = pull(socketChannel);

		///Si null cel� signifie que le flux est termin�
		if (dataReceived != null) {
			//		if (dataReceived.startsWith("!")) {
			//			eventListener.onEvent(dataReceived.substring(1));
			//		}
			//System.out.println("$execCommand 2 [dataReceived]: " + dataReceived);
			final VResponse response = onDataReceived(dataReceived, requestHandler);
			//System.out.println("$execCommand 3 [response]: " + response);
			if (response.hasError()) {
				push(socketChannel, "-" + response.getErrorMsg());
			} else {
				push(socketChannel, "+" + response.getResponse());
			}
		}
	}

	private VResponse onDataReceived(String dataReceived, VCommandHandler commandHandler) {
		Assertion.checkNotNull(dataReceived);
		//---------------------------------------------------------------------
		if (!dataReceived.startsWith("$")) {
			return VResponse.createResponseWithError("command must start with $");
		}
		try {
			return commandHandler.onCommand(new VCommand(dataReceived.substring(1)));
		} catch (RuntimeException e) {
			return VResponse.createResponseWithError("Error :" + e.getMessage());
		}
	}
}
