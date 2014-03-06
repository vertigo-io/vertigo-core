package io.vertigo.ccc.json;

import static spark.Spark.get;
import io.vertigo.ccc.console.VConsoleHandler;
import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VResponse;

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
