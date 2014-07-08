package io.vertigo.rest.handler;

import io.vertigo.kernel.Home;
import io.vertigo.rest.EndPointDefinition;
import io.vertigo.rest.RestfulService;
import spark.Request;
import spark.Response;

/**
 * Exceptions handler. Convert exception to response.
 * @author npiedeloup
 */
final class RestfulServiceHandler implements RouteHandler {

	private final EndPointDefinition endPointDefinition;

	public RestfulServiceHandler(final EndPointDefinition endPointDefinition) {
		this.endPointDefinition = endPointDefinition;
	}

	/** {@inheritDoc} */
	public Object handle(final Request request, final Response response, final HandlerChain chain) {
		final RestfulService service = (RestfulService) Home.getComponentSpace().resolve(endPointDefinition.getMethod().getDeclaringClass());
		final Object value = RestfulServicesUtil.invoke(service,//
				endPointDefinition.getMethod(), request);
		return RestfulServicesUtil.toJson(value);
	}
}
