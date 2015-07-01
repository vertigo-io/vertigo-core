package io.vertigo.vega.plugins.rest.handler.reader;

import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;

import java.util.Arrays;

import spark.Request;

public final class BodyJsonReader implements JsonReader<String> {

	/** {@inheritDoc} */
	@Override
	public RestParamType[] getSupportedInput() {
		return new RestParamType[] { RestParamType.Body };
	}

	/** {@inheritDoc} */
	@Override
	public Class<String> getSupportedOutput() {
		return String.class;
	}

	/** {@inheritDoc} */
	@Override
	public String extractData(final Request request, final EndPointParam endPointParam, final RouteContext routeContext) {
		Assertion.checkArgument(getSupportedInput()[0].equals(endPointParam.getParamType()), "This JsonReader can't read the asked request ParamType {0}. Only {1} is supported", endPointParam.getParamType(), Arrays.toString(getSupportedInput()));
		//-----
		return request.body();
	}

}
