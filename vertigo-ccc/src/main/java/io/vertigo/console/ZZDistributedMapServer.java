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
import io.vertigoimpl.engines.command.tcp.VServer;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Mock server to test Console.
 * @author pchretien
 */
public class ZZDistributedMapServer {
	private final VServer tcpServer2;

	public static void main(String[] args) {
		ZZDistributedMapServer server = new ZZDistributedMapServer();
		server.start();
	}

	private void start() {
		new Thread(tcpServer2).start();
	}

	private ZZDistributedMapServer() {
		tcpServer2 = new VServer(new MyCommandHandler(), 4444);
	}

	private static class MyCommandHandler implements VCommandHandler {
		private final Gson gson = new GsonBuilder().create();
		private final Map<String, String> map = new HashMap<>();

		public VResponse onCommand(VCommand command) {
			System.out.println(">>ici>" + command);
			try {
				if ("put".equals(command.getName())) {
					String key = command.arg("name", (String) null);
					String value = command.arg("value", (String) null);
					map.put(key, value);
					return VResponse.createResponse(gson.toJson("OK"));
				} else if ("keys".equals(command.getName())) {
					return VResponse.createResponse(gson.toJson(map.keySet()));
				} else if ("values".equals(command.getName())) {
					return VResponse.createResponse(gson.toJson(map.values()));
				} else if ("get".equals(command.getName())) {
					String key = command.arg("name", (String) null);
					return VResponse.createResponse(gson.toJson(map.get(key)));
				} else if ("clear".equals(command.getName())) {
					map.clear();
					return VResponse.createResponse(gson.toJson("OK"));
				}
				return VResponse.createResponseWithError("unknown command " + command.getName());
			} catch (Exception e) {
				return VResponse.createResponseWithError(command.getName() + " failed " + e);
			}
		}
	}
}
