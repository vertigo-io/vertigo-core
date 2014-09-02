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
package io.vertigo.ccc.json;

import static spark.Spark.get;
import io.vertigo.ccc.console.VConsoleHandler;
import io.vertigo.core.command.VCommand;
import io.vertigo.core.command.VResponse;

import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;

public final class Hub {

	public static void main(String[] args) {
		final VConsoleHandler consoleHandler = new VConsoleHandler();

		get(new Route("/:cmd") {
			@Override
			public Object handle(Request request, Response response) {
				final String commandName = request.params(":cmd");
				Map<String, String> params = new HashMap<>();
				for (String queryParam : request.queryParams()) {
					params.put(queryParam, request.queryParams(queryParam));
				}
				VResponse vresponse = consoleHandler.execCommand(new VCommand(commandName, params));
				return vresponse.getResponse();
			}
		});
	}
}
