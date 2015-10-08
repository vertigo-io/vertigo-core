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

import io.vertigo.core.command.VCommand;
import io.vertigo.core.command.VResponse;
import io.vertigo.engines.command.JsonUtil;
import io.vertigo.engines.command.tcp.VClient;
import io.vertigo.lang.Activeable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author pchretien
 */
public final class VConsoleHandler implements Activeable {
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 4444;
	private final Map<SocketAddress, VClient> clients = new LinkedHashMap<>();

	@Override
	public void start() {
		//		clients.p = new VClient("localhost", 4444);
	}

	@Override
	public void stop() {
		final Iterator<VClient> it = clients.values().iterator();
		while (it.hasNext()) {
			it.next().close();
			it.remove();
		}
	}

	public VResponse execCommand(final VCommand command) {
		if (command.getName().startsWith("$")) {
			switch (command.getName()) {
				case "$help":
					return VResponse.createResponse("toto");
				case "$who":
					return VResponse.createResponse(JsonUtil.toJson(clients.keySet()));
				case "$disconnect":
					stop();
					return VResponse.createResponse(JsonUtil.toJson("disconnection OK"));
				case "$connect":
					final String host = command.arg("host", DEFAULT_HOST);
					final int port = command.arg("port", DEFAULT_PORT);
					final SocketAddress socketAddress = new InetSocketAddress(host, port);
					if (clients.containsKey(socketAddress)) {
						return VResponse.createResponseWithError("connection already established");
					}

					try {
						final VClient client = new VClient(socketAddress);
						clients.put(socketAddress, client);
						return VResponse.createResponse(JsonUtil.toJson("connection successfull"));
					} catch (final Exception e) {
						return VResponse.createResponseWithError("connection failed " + e.getMessage());
					}
				default:
					return VResponse.createResponseWithError("$command unknow " + command.getName());
			}
		}
		if (clients.isEmpty()) {
			return VResponse.createResponseWithError("you are not connected, try $help");
		}
		//
		final JsonParser parser = new JsonParser();

		//		} else if (clients.size() == 1) {
		//			return clients.get(0).execCommand(command);
		//		} else {
		//Multiples clients
		final JsonObject map = new JsonObject();
		for (final Entry<SocketAddress, VClient> entry : clients.entrySet()) {
			final VResponse response = entry.getValue().execCommand(command);
			if (response.hasError()) {
				//if only one response has an error...
				return response;
			}
			final JsonElement jsonElement = parser.parse(response.getResponse());
			map.add(entry.getKey().toString(), jsonElement);
		}
		return VResponse.createResponse(map.toString());
	}
}
