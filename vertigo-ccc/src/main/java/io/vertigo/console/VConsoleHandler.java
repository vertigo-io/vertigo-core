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
package io.vertigo.console;

import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VCommandHandler;
import io.vertigo.kernel.command.VResponse;
import io.vertigo.kernel.lang.Activeable;
import io.vertigoimpl.engines.command.json.JsonAdapter;
import io.vertigoimpl.engines.command.tcp.VClient;

import java.util.ArrayList;
import java.util.List;

public final class VConsoleHandler implements VCommandHandler, Activeable {
	private final JsonAdapter jsonAdapter = new JsonAdapter();
	private List<VClient> clients = new ArrayList<>();

	VConsoleHandler() {
		//
	}

	public void start() {
		//		clients.p = new VClient("localhost", 4444);
	}

	public void stop() {
		for (VClient client : clients) {
			client.close();
		}
	}

	@Override
	public VResponse onCommand(VCommand command) {
		if (command.getName().startsWith("$")) {
			switch (command.getName()) {
				case "$help":
					return VResponse.createResponse("toto");
					//				case "$reconnect":
					//					close(client);
					//					client = new VClient("localhost", 4444);
					//					return VResponse.createResponse(jsonAdapater.toJson("connection OK"));
				case "$who":
					List<String> remoteAddresses = new ArrayList<>();
					for (VClient client : clients) {
						remoteAddresses.add(client.getRemoteAddress());
					}
					return VResponse.createResponse(jsonAdapter.toJson(remoteAddresses));
				case "$disconnect":
					stop();
					return VResponse.createResponse(jsonAdapter.toJson("disconnection OK"));
				case "$connect":
					//					if (client != null) {
					//						return VResponse.createResponseWithError("you are already connected");
					//					}
					try {
						String host = command.arg("host", "localhost");
						int port = command.arg("port", 4444);

						VClient client = new VClient(host, port);
						clients.add(client);
						return VResponse.createResponse(jsonAdapter.toJson("connection successfull"));
					} catch (Exception e) {
						return VResponse.createResponseWithError("connection failed " + e.getMessage());
					}
				default:
					return VResponse.createResponseWithError("$command unknow " + command.getName());
			}
		}
		if (clients.isEmpty()) {
			return VResponse.createResponseWithError("you are not connected");
		} else if (clients.size() == 1) {
			return clients.get(0).onCommand(command);
		} else {
			//Multiples clients
			List<VResponse> responses = new ArrayList<>();
			for (VClient client : clients) {
				responses.add(client.onCommand(command));
			}
			return VResponse.createResponse(jsonAdapter.toJson(responses));
		}
	}
}
