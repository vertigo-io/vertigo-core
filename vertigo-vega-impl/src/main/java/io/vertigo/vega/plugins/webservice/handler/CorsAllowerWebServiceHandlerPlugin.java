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
package io.vertigo.vega.plugins.webservice.handler;

import io.vertigo.lang.Option;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.exception.VSecurityException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;

/**
 * Handler of Cross-Origin Resource Sharing (CORS).
 * @author npiedeloup
 */
public final class CorsAllowerWebServiceHandlerPlugin implements WebServiceHandlerPlugin {

	private static final String REQUEST_HEADER_ORIGIN = "Origin";

	private static final String DEFAULT_ORIGIN_CORS_FILTER = "*";
	private static final String DEFAULT_METHODS_CORS_FILTER = "GET, POST, DELETE, PUT, OPTIONS"; // may use *
	private static final String DEFAULT_HEADERS_CORS_FILTER = "Content-Type, listServerToken, x-total-count, x-access-token"; // may use *

	private final String originCORSFilter;
	private final String methodCORSFilter;
	private final Set<String> originCORSFiltersSet;
	private final Set<String> methodCORSFiltersSet;

	/**
	 * @param originCORSFilter Origin CORS Allowed
	 * @param methodCORSFilter Method CORS Allowed
	 */
	@Inject
	public CorsAllowerWebServiceHandlerPlugin(@Named("originCORSFilter") final Option<String> originCORSFilter, @Named("methodCORSFilter") final Option<String> methodCORSFilter) {
		this.originCORSFilter = originCORSFilter.getOrElse(DEFAULT_ORIGIN_CORS_FILTER);
		this.methodCORSFilter = methodCORSFilter.getOrElse(DEFAULT_METHODS_CORS_FILTER);
		originCORSFiltersSet = parseStringToSet(this.originCORSFilter);
		methodCORSFiltersSet = parseStringToSet(this.methodCORSFilter);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException, VSecurityException {
		final String origin = request.headers(REQUEST_HEADER_ORIGIN);
		if (origin != null) {
			final String method = request.raw().getMethod();
			if (!isAllowed(origin, originCORSFiltersSet) || !isAllowed(method, methodCORSFiltersSet)) {
				response.status(HttpServletResponse.SC_FORBIDDEN);
				response.raw().resetBuffer();
				throw new VSecurityException("Invalid CORS Access (Origin:" + origin + ", Method:" + method + ")");
			}
		}
		response.header("Access-Control-Allow-Origin", originCORSFilter);
		response.header("Access-Control-Request-Method", methodCORSFilter);
		response.header("Access-Control-Allow-Headers", "Cache-Control, X-Requested-With");
		response.header("Access-Control-Expose-Headers", DEFAULT_HEADERS_CORS_FILTER);
		return chain.handle(request, response, routeContext);
	}

	private static boolean isAllowed(final String currentValue, final Set<String> allowedValues) {
		if (allowedValues.contains("*")) {
			return true;
		}
		return allowedValues.contains(currentValue);
	}

	private static Set<String> parseStringToSet(final String param) {
		final String[] values = param.split(",");
		final Set<String> parsedConfig = new HashSet<>(values.length);
		for (final String value : values) {
			parsedConfig.add(value.trim());
		}
		return parsedConfig;
	}
}
