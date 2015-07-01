package io.vertigo.vega.plugins.rest.handler.converter;

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.engine.UiContext;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointParam;

import java.util.Arrays;

import javax.inject.Inject;

public final class DefaultJsonConverter implements JsonConverter {

	private final JsonEngine jsonReaderEngine;

	/**
	 * @param jsonReaderEngine jsonReaderEngine
	 */
	@Inject
	public DefaultJsonConverter(final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		this.jsonReaderEngine = jsonReaderEngine;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return !VFile.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc}*/
	@Override
	public void populateRouteContext(final Object input, final EndPointParam endPointParam, final RouteContext routeContext) throws VSecurityException {
		Assertion.checkArgument(getSupportedInputs()[0].isInstance(input) || getSupportedInputs()[1].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Class<?> paramClass = endPointParam.getType();
		final Object value;
		if (input instanceof String) {
			value = jsonReaderEngine.fromJson((String) input, endPointParam.getType());
		} else if (input instanceof UiContext) {
			value = ((UiContext) input).get(endPointParam.getName());
		} else {
			throw new IllegalArgumentException(String.format("This JsonConverter can't read the asked type %s. Only %s is supported", paramClass.getSimpleName(), UiListDelta.class.getSimpleName()));
		}//-----
		routeContext.setParamValue(endPointParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { String.class, UiContext.class };
	}
}
