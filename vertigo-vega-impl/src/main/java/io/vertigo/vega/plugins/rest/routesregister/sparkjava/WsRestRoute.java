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
package io.vertigo.vega.plugins.rest.routesregister.sparkjava;

import io.vertigo.vega.plugins.rest.handler.HandlerChain;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Webservice Route for Spark.
 * @author npiedeloup
 */
final class WsRestRoute extends Route {

	private static final Logger LOGGER = Logger.getLogger(WsRestRoute.class);

	private final EndPointDefinition endPointDefinition;
	private final HandlerChain handlerChain;
	private final String defaultContentCharset;

	/**
	 * @param endPointDefinition endPointDefinition
	 * @param handlerChain handlerChain
	 * @param defaultContentCharset DefaultContentCharset
	 */
	WsRestRoute(final EndPointDefinition endPointDefinition, final HandlerChain handlerChain, final String defaultContentCharset) {
		super(convertJaxRsPathToSpark(endPointDefinition.getPath()), endPointDefinition.getAcceptType());
		this.endPointDefinition = endPointDefinition;
		this.handlerChain = handlerChain;
		this.defaultContentCharset = defaultContentCharset;
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response) {
		try {
			final Request requestWrapper = new SparkRequestWrapper(request, defaultContentCharset);
			return handlerChain.handle(requestWrapper, response, new RouteContext(requestWrapper, response, endPointDefinition));
		} catch (final Throwable th) {
			LOGGER.error(th);
			return th.getMessage();
		}
	}

	private static String convertJaxRsPathToSpark(final String path) {
		return path.replaceAll("\\{(.+?)\\}", ":$1"); //.+? : Reluctant regexp
	}
}
