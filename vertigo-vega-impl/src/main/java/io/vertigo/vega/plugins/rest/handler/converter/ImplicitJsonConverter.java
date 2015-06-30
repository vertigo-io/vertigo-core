package io.vertigo.vega.plugins.rest.handler.converter;

import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.vega.rest.validation.UiMessageStack;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spark.Request;

public final class ImplicitJsonConverter implements JsonConverter {

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return UiMessageStack.class.isAssignableFrom(paramClass)
				|| HttpServletRequest.class.isAssignableFrom(paramClass)
				|| HttpServletResponse.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public void populateRouteContext(final Object input, final EndPointParam endPointParam, final RouteContext routeContext) {
		Assertion.checkArgument(getSupportedInputs()[0].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Object value = readImplicitValue((Request) input, endPointParam, routeContext);
		routeContext.setParamValue(endPointParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { Request.class };
	}

	private Object readImplicitValue(final Request request, final EndPointParam endPointParam, final RouteContext routeContext) {
		switch (ImplicitParam.valueOf(endPointParam.getName())) {
			case UiMessageStack:
				return routeContext.getUiMessageStack();
			case Request:
				return request.raw();
			case Response:
				return routeContext.getResponse().raw();
			default:
				throw new IllegalArgumentException("ImplicitParam : " + endPointParam.getName());
		}
	}

}
