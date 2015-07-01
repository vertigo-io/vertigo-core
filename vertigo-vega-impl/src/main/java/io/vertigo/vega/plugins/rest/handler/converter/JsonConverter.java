package io.vertigo.vega.plugins.rest.handler.converter;

import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointParam;

/**
 * Converter source object into value object and put it into RouteContext.
 * @author npiedeloup
 */
public interface JsonConverter {

	/**
	 * @param paramClass Class to test
	 * @return If this converter can output this type of data.
	 */
	boolean canHandle(Class<?> paramClass);

	/**
	 * Converter source object into value object and put it into RouteContext.
	 * @param source Source
	 * @param endPointParam Param
	 * @param routeContext RouteContext
	 * @throws VSecurityException Security exception
	 */
	void populateRouteContext(Object source, EndPointParam endPointParam, RouteContext routeContext) throws VSecurityException;

	/**
	 * @return Input types
	 */
	Class[] getSupportedInputs();

}
