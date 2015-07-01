package io.vertigo.vega.plugins.rest.handler.reader;

import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import spark.Request;

/**
 * Read request to extract a not converted parameter.
 * @author npiedeloup
 * @param <O> Output type
 */
public interface JsonReader<O> {

	/**
	 * @return Supported type of parameter in request
	 */
	RestParamType[] getSupportedInput();

	/**
	 * @return Output classe supported
	 */
	Class<O> getSupportedOutput();

	/**
	 * Extract parameter value from request as readType.
	 * This doesn't convert it to value object, it's only extraction, the converter do the convert task.
	 * @param request Request
	 * @param endPointParam Param infos
	 * @param routeContext routeContext
	 * @return output value
	 */
	O extractData(Request request, EndPointParam endPointParam, RouteContext routeContext);

}
