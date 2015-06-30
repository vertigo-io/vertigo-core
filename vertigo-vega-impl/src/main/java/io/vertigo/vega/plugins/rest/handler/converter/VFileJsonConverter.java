package io.vertigo.vega.plugins.rest.handler.converter;

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.metamodel.EndPointParam;

import java.util.Arrays;

import spark.Request;

public final class VFileJsonConverter implements JsonConverter {

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return VFile.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public void populateRouteContext(final Object input, final EndPointParam endPointParam, final RouteContext routeContext) {
		Assertion.checkArgument(getSupportedInputs()[0].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final VFile value = VFileUtil.readVFileParam((Request) input, endPointParam);
		routeContext.setParamValue(endPointParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { Request.class };
	}

}
