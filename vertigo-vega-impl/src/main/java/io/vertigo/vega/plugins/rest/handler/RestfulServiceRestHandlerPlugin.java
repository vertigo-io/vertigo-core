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
package io.vertigo.vega.plugins.rest.handler;

import io.vertigo.core.Home;
import io.vertigo.lang.VUserException;
import io.vertigo.util.ClassUtil;
import io.vertigo.vega.impl.rest.RestHandlerPlugin;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.model.UiListState;
import io.vertigo.vega.rest.validation.ValidationUserException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;

/**
 * RestfulServiceHandler : call service method.
 * @author npiedeloup
 */
public final class RestfulServiceRestHandlerPlugin implements RestHandlerPlugin {

	/** {@inheritDoc} */
	@Override
	public boolean accept(final EndPointDefinition endPointDefinition) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws SessionException, VSecurityException {
		final EndPointDefinition endPointDefinition = routeContext.getEndPointDefinition();
		final Object[] serviceArgs = makeArgs(routeContext, endPointDefinition);
		final Method method = endPointDefinition.getMethod();
		final RestfulService service = (RestfulService) Home.getComponentSpace().resolve(method.getDeclaringClass());

		if (method.getName().startsWith("create")) { //by convention, if method start with 'create', we return http 201 status code (if ok)
			response.status(HttpServletResponse.SC_CREATED);
		}
		try {
			return ClassUtil.invoke(service, method, serviceArgs);
		} catch (final RuntimeException e) {
			//If throwed exception was ValidationUserException, VUserException, SessionException, VSecurityException, RuntimeException
			//we re throw it
			final Throwable cause = e.getCause();
			if (cause instanceof InvocationTargetException) {
				final Throwable targetException = ((InvocationTargetException) cause).getTargetException();
				if (targetException instanceof ValidationUserException) {
					throw (ValidationUserException) targetException;
				} else if (targetException instanceof VUserException) {
					throw (VUserException) targetException;
				} else if (targetException instanceof SessionException) {
					throw (SessionException) targetException;
				} else if (targetException instanceof VSecurityException) {
					throw (VSecurityException) targetException;
				} else if (targetException instanceof RuntimeException) {
					throw (RuntimeException) targetException;
				}
			}
			throw e;
		}
	}

	private static Object[] makeArgs(final RouteContext routeContext, final EndPointDefinition endPointDefinition) {
		if (endPointDefinition.isAutoSortAndPagination()) {
			final Object[] serviceArgs = new Object[endPointDefinition.getEndPointParams().size() - 1];
			int i = 0;
			for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
				if (!endPointParam.getType().isAssignableFrom(UiListState.class)) {
					serviceArgs[i] = routeContext.getParamValue(endPointParam);
					i++;
				}
			}
			return serviceArgs;
		}
		final Object[] serviceArgs = new Object[endPointDefinition.getEndPointParams().size()];
		int i = 0;
		for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
			serviceArgs[i] = routeContext.getParamValue(endPointParam);
			i++;
		}
		return serviceArgs;
	}
}
