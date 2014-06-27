package io.vertigo.rest.handler;

import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;
import spark.Request;
import spark.Response;

/**
 * Atomic handler of http request. 
 * @author npiedeloup
 */
public interface RouteHandler {

	/**
	 * Do handle of this route.
	 * 
	 * @param request spark.Request
	 * @param response spark.Response
	 * @param chain current HandlerChain.
	 * @return Response body
	 * @throws SessionException Session expired exception
	 * @throws VSecurityException Security exception
	 */
	Object handle(final Request request, final Response response, final HandlerChain chain) throws SessionException, VSecurityException;
}
