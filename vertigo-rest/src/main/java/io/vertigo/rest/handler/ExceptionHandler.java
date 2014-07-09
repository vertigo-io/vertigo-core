/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
