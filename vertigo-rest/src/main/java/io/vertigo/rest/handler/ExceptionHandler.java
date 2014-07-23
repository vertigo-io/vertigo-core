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
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.rest.engine.JsonEngine;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.TooManyRequestException;
import io.vertigo.rest.exception.VSecurityException;
import io.vertigo.rest.validation.UiMessageStack;
import io.vertigo.rest.validation.ValidationUserException;

import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;

import com.google.gson.JsonSyntaxException;

/**
 * Exceptions handler. Convert exception to response.
 * @author npiedeloup
 */
public final class ExceptionHandler implements RouteHandler {
	private final JsonEngine jsonWriterEngine;
	private static final int SC_UNPROCESSABLE_ENTITY = 422; //server understands the content syntaxe but not semanticly
	private static final int SC_TOO_MANY_REQUEST = 429; //RFC 6585 : TooManyRequest in time window

	ExceptionHandler(final JsonEngine jsonWriterEngine) {
		Assertion.checkNotNull(jsonWriterEngine);
		//---------------------------------------------------------------------
		this.jsonWriterEngine = jsonWriterEngine;
	}

	/** {@inheritDoc} */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) {
		try {
			return chain.handle(request, response, routeContext);
		} catch (final ValidationUserException e) {
			//response.status(HttpServletResponse.SC_BAD_REQUEST);
			response.status(SC_UNPROCESSABLE_ENTITY);
			final UiMessageStack uiMessageStack = routeContext.getUiMessageStack();
			e.flushToUiMessageStack(uiMessageStack);
			return jsonWriterEngine.toJson(uiMessageStack);
		} catch (final VUserException e) {
			//response.status(HttpServletResponse.SC_BAD_REQUEST);
			response.status(SC_UNPROCESSABLE_ENTITY);
			return jsonWriterEngine.toJsonError(e);
		} catch (final SessionException e) {
			response.status(HttpServletResponse.SC_UNAUTHORIZED);
			return jsonWriterEngine.toJsonError(e);
		} catch (final VSecurityException e) {
			response.status(HttpServletResponse.SC_FORBIDDEN);
			return jsonWriterEngine.toJsonError(e);
		} catch (final JsonSyntaxException e) {
			response.status(HttpServletResponse.SC_BAD_REQUEST);
			e.printStackTrace();//TODO use a loggers
			return jsonWriterEngine.toJsonError(e);
		} catch (final TooManyRequestException e) {
			response.status(SC_TOO_MANY_REQUEST);
			return jsonWriterEngine.toJsonError(e);
		} catch (final Throwable e) {
			response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();//TODO use a loggers
			return jsonWriterEngine.toJsonError(e);
		}
	}
}
