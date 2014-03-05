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
package io.vertigo.ccc.console;

import io.vertigo.engines.command.JsonUtil;
import io.vertigo.engines.command.tcp.VClient;
import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VResponse;
import io.vertigo.kernel.lang.Activeable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author pchretien
 */
final class VConsoleHandler implements Activeable {
	private Map<SocketAddress, VClient> clients = new LinkedHashMap<>();

	public void start() {
		//		clients.p = new VClient("localhost", 4444);
	}

	public void stop() {
		final Iterator<VClient> it = clients.values().iterator();
		while (it.hasNext()) {
			it.next().close();
			it.remove();
		}
	}

	VResponse execCommand(VCommand command) {
		if (command.getName().startsWith("$")) {
			switch (command.getName()) {
				case "$help":
					return VResponse.createResponse("toto");
					//				case "$reconnect":
					//					close(client);
					//					client = new VClient("localhost", 4444);
					//					return VResponse.createResponse(jsonAdapater.toJson("connection OK"));
				case "$who":
					//					List<String> remoteAddresses = new ArrayList<>();
					//					for (VClient client : clients) {
					//						remoteAddresses.add(client.getRemoteAddress());
					//					}
					return VResponse.createResponse(JsonUtil.toJson(clients.keySet()));
				case "$disconnect":
					stop();
					return VResponse.createResponse(JsonUtil.toJson("disconnection OK"));
				case "$connect":
					//					if (client != null) {
					//						return VResponse.createResponseWithError("you are already connected");
					//					}
					String host = command.arg("host", "localhost");
					int port = command.arg("port", 4400);
					SocketAddress socketAddress = new InetSocketAddress(host, port);
					if (clients.containsKey(socketAddress)) {
						return VResponse.createResponseWithError("connection already established");
					}

					try {
						VClient client = new VClient(socketAddress);
						clients.put(socketAddress, client);
						return VResponse.createResponse(JsonUtil.toJson("connection successfull"));
					} catch (Exception e) {
						return VResponse.createResponseWithError("connection failed " + e.getMessage());
					}
				default:
					return VResponse.createResponseWithError("$command unknow " + command.getName());
			}
		}
		if (clients.isEmpty()) {
			return VResponse.createResponseWithError("you are not connected");
		} //		} else if (clients.size() == 1) {
			//			return clients.get(0).execCommand(command);
			//		} else {
			//Multiples clients
		Map<SocketAddress, VResponse> responses = new HashMap<>();
		for (Entry<SocketAddress, VClient> entry : clients.entrySet()) {
			responses.put(entry.getKey(), entry.getValue().execCommand(command));
		}
		return VResponse.createResponse(JsonUtil.toJson(responses));
	}
}
