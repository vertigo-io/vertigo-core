package io.vertigo.rest.handler;

import io.vertigo.kernel.Home;
import io.vertigo.rest.EndPointDefinition;
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
		final Object value = RestfulServicesUtil.invoke( //
				Home.getComponentSpace().resolve(endPointDefinition.getMethod().getDeclaringClass()),//
				endPointDefinition.getMethod(), request);
		return RestfulServicesUtil.toJson(value);
	}
}
