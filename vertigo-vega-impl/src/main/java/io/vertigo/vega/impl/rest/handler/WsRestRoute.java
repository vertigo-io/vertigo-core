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
package io.vertigo.vega.impl.rest.handler;

import io.vertigo.core.Home;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.vega.rest.engine.GoogleJsonEngine;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.token.TokenManager;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Webservice Route for Spark.
 * @author npiedeloup
 */
public final class WsRestRoute extends Route {

	private final Logger logger = Logger.getLogger(getClass());

	@Inject
	private RateLimitingHandler rateLimitingHandler;
	@Inject
	private KSecurityManager securityManager;
	@Inject
	private TokenManager uiSecurityTokenManager;

	private final HandlerChain handlerChain;
	private final JsonEngine jsonEngine = new GoogleJsonEngine();
	private final String defaultContentCharset;

	/**
	 * @param endPointDefinition EndPoint Definition
	 * @param defaultContentCharset DefaultContentCharset
	 */
	public WsRestRoute(final EndPointDefinition endPointDefinition, final String defaultContentCharset) {
		super(convertJaxRsPathToSpark(endPointDefinition.getPath()), endPointDefinition.getAcceptType());
		Injector.injectMembers(this, Home.getComponentSpace());
		this.defaultContentCharset = defaultContentCharset;

		handlerChain = new HandlerChainBuilder()
				.withHandler(new ExceptionHandler(jsonEngine))
				.withHandler(endPointDefinition.isSessionInvalidate(), new SessionInvalidateHandler())
				.withHandler(endPointDefinition.isNeedSession(), new SessionHandler(securityManager))
				.withHandler(rateLimitingHandler)
				.withHandler(endPointDefinition.isNeedAuthentification(), new SecurityHandler(securityManager))
				.withHandler(new AccessTokenHandler(uiSecurityTokenManager, endPointDefinition))
				.withHandler(new JsonConverterHandler(uiSecurityTokenManager, endPointDefinition, jsonEngine, jsonEngine))
				.withHandler(new ValidatorHandler(endPointDefinition))
				.withHandler(new RestfulServiceHandler(endPointDefinition))
				.build();
	}

	private static String convertJaxRsPathToSpark(final String path) {
		final String newPath = path.replaceAll("\\{(.+?)\\}", ":$1"); //.+? : Reluctant regexp
		return newPath;
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response) {
		try {
			final Request requestWrapper = new SparkRequestWrapper(request, defaultContentCharset);
			return handlerChain.handle(requestWrapper, response, new RouteContext(requestWrapper));
		} catch (final Throwable th) {
			logger.error(th);
			return th.getMessage();
		}
	}

}
