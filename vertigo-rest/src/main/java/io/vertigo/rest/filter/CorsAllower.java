package io.vertigo.rest.filter;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Handler of Cross-Origin Resource Sharing (CORS).
 * @author npiedeloup
 */
public class CorsAllower extends Filter {
	private final String originCORSFilter = "*";
	private final String methodsCORSFilter = "*";
	private final String headersCORSFilter = "*";

	/** {@inheritDoc} */
	@Override
	public void handle(final Request request, final Response response) {
		response.header("Access-Control-Allow-Origin", originCORSFilter);
		response.header("Access-Control-Request-Method", methodsCORSFilter);
		response.header("Access-Control-Allow-Headers", headersCORSFilter);
	}

}
