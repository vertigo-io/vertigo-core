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

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.rest.engine.GoogleJsonEngine;
import io.vertigo.rest.engine.JsonEngine;
import io.vertigo.rest.metamodel.EndPointDefinition;
import io.vertigo.rest.security.UiSecurityTokenManager;

import javax.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Webservice Route for Spark.
 * @author npiedeloup
 */
public final class WsRestRoute extends Route {

	//	private ExceptionHandler exceptionHandler;
	//	private SessionHandler sessionHandler;
	//	private SecurityHandler securityHandler;
	@Inject
	private RateLimitingHandler rateLimitingHandler;
	@Inject
	private KSecurityManager securityManager;
	@Inject
	private UiSecurityTokenManager uiSecurityTokenManager;

	private final HandlerChain handlerChain = new HandlerChain();
	private final JsonEngine jsonEngine = new GoogleJsonEngine();

	public WsRestRoute(final EndPointDefinition endPointDefinition) {
		super(convertJaxRsPathToSpark(endPointDefinition.getPath()), endPointDefinition.getAcceptType());
		new Injector().injectMembers(this, Home.getComponentSpace());

		handlerChain.addHandler(new ExceptionHandler(jsonEngine));
		if (endPointDefinition.isNeedSession()) {
			handlerChain.addHandler(new SessionHandler(securityManager));
		}
		handlerChain.addHandler(rateLimitingHandler);
		if (endPointDefinition.isNeedAuthentification()) {
			handlerChain.addHandler(new SecurityHandler(securityManager));
		}
		handlerChain.addHandler(new AccessTokenHandler(uiSecurityTokenManager, endPointDefinition));
		handlerChain.addHandler(new JsonConverterHandler(uiSecurityTokenManager, endPointDefinition, jsonEngine, jsonEngine));
		handlerChain.addHandler(new KFileHandler());
		handlerChain.addHandler(new ValidatorHandler(endPointDefinition));
		handlerChain.addHandler(new RestfulServiceHandler(endPointDefinition));
	}

	private static String convertJaxRsPathToSpark(final String path) {
		final String newPath = path.replaceAll("(.*)\\{(.+)\\}(.*)", "$1:$2$3");
		return newPath;
	}

	@Override
	public Object handle(final Request request, final Response response) {
		try {
			return handlerChain.handle(request, response, new RouteContext(request));
		} catch (final Throwable th) {
			System.err.println("Error " + th.getMessage());
			th.printStackTrace(System.err);
			return th.getMessage();
		}
	}
}
