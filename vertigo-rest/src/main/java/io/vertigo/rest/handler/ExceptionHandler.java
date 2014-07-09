package io.vertigo.rest.handler;

import io.vertigo.kernel.exception.VUserException;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;

import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;

/**
 * Exceptions handler. Convert exception to response.
 * @author npiedeloup
 */
public final class ExceptionHandler implements RouteHandler {

	/** {@inheritDoc} */
	public Object handle(final Request request, final Response response, final HandlerChain chain) {
		try {
			response.type("application/json;charset=UTF-8");

			return chain.handle(request, response);
		} catch (final VUserException e) {
			response.status(HttpServletResponse.SC_BAD_REQUEST);
			return RestfulServicesUtil.toJsonError(e.getMessage());
		} catch (final SessionException e) {
			response.status(HttpServletResponse.SC_UNAUTHORIZED);
			return RestfulServicesUtil.toJsonError(e.getMessage());
		} catch (final VSecurityException e) {
			response.status(HttpServletResponse.SC_FORBIDDEN);
			return RestfulServicesUtil.toJsonError(e.getMessage());
		} catch (final Throwable e) {
			response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();//TODO use a loggers
			return RestfulServicesUtil.toJsonError(e.getMessage());
		}
	}
}
