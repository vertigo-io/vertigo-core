package io.vertigo.vega.plugins.rest.handler.reader;

import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import spark.Request;

public final class DefaultJsonReader implements JsonReader<Request> {

	/** {@inheritDoc} */
	@Override
	public RestParamType[] getSupportedInput() {
		return RestParamType.values(); //default support all
	}

	/** {@inheritDoc} */
	@Override
	public Class<Request> getSupportedOutput() {
		return Request.class;
	}

	/** {@inheritDoc} */
	@Override
	public Request extractData(final Request request, final EndPointParam endPointParam, final RouteContext routeContext) {
		return request;
	}
}
