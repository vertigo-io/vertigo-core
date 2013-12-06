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

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Mock server to test Console.
 * @author pchretien
 */
public class MockServer {
	private final VServer tcpServer2;

	public static void main(String[] args) {
		MockServer server = new MockServer(4444);
		MockServer server2 = new MockServer(4443);
		server.start();
		server2.start();
	}

	private void start() {
		new Thread(tcpServer2).start();
	}

	private MockServer(int port) {
		final Gson gson = new GsonBuilder().create();
		tcpServer2 = new VServer(new VCommandHandler() {
			@Override
			public VResponse onCommand(VCommand command) {
				switch (command.getName()) {
					case "help":
						return VResponse.createResponse(gson.toJson("i need somebody"));
					case "ping":
						return VResponse.createResponse(gson.toJson("pong"));
					case "pong":
						return VResponse.createResponse(gson.toJson("ping"));
					case "date":
						return VResponse.createResponse(gson.toJson(new Date()));
					default:
						return VResponse.createResponseWithError("unknown command:" + command.getName());
				}
			}
		}, port);
	}

}
