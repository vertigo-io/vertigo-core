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
package io.vertigo.vega.impl.rest;

import io.vertigo.core.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.impl.rest.handler.AccessTokenHandler;
import io.vertigo.vega.impl.rest.handler.CorsAllowerHandler;
import io.vertigo.vega.impl.rest.handler.ExceptionHandler;
import io.vertigo.vega.impl.rest.handler.HandlerChain;
import io.vertigo.vega.impl.rest.handler.JsonConverterHandler;
import io.vertigo.vega.impl.rest.handler.PaginatorAndSortHandler;
import io.vertigo.vega.impl.rest.handler.RateLimitingHandler;
import io.vertigo.vega.impl.rest.handler.RestfulServiceHandler;
import io.vertigo.vega.impl.rest.handler.SecurityHandler;
import io.vertigo.vega.impl.rest.handler.SessionHandler;
import io.vertigo.vega.impl.rest.handler.SessionInvalidateHandler;
import io.vertigo.vega.rest.EndPointIntrospectorPlugin;
import io.vertigo.vega.rest.RestManager;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.xml.validation.ValidatorHandler;

/**
 * Restful webservice manager.
 * Use some plugins :
 * - EndPointIntrospectorPlugin : introspect WebService and register EndPointDefinitions
 * - RoutesRegisterPlugin : Register EndPointDefinitions to Routing engine (Jersey, Spark or other)
 * - List<RestHandlerPlugin> : Ordered handlers list to managed : request to WebService impl and callback response
 *
 * @author npiedeloup
 */
public final class RestManagerImpl implements RestManager {

	private static final String STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG = "Standard configuration (order is important) :\n"
			+ "- " + ExceptionHandler.class.getSimpleName() + "\n"
			+ "- " + CorsAllowerHandler.class.getSimpleName() + "\n"
			+ "- " + SessionInvalidateHandler.class.getSimpleName() + "\n"
			+ "- " + SessionHandler.class.getSimpleName() + "\n"
			+ "- " + RateLimitingHandler.class.getSimpleName() + "\n"
			+ "- " + SecurityHandler.class.getSimpleName() + "\n"
			+ "- " + AccessTokenHandler.class.getSimpleName() + "\n"
			+ "- " + JsonConverterHandler.class.getSimpleName() + "\n"
			+ "- " + PaginatorAndSortHandler.class.getSimpleName() + "\n"
			+ "- " + ValidatorHandler.class.getSimpleName() + "\n"
			+ "- " + RestfulServiceHandler.class.getSimpleName() + "\n";

	private final EndPointIntrospectorPlugin endPointIntrospectorPlugin;
	private final RoutesRegisterPlugin routesRegisterPlugin;
	private final HandlerChain handlerChain;

	//private final JsonEngine jsonEngine = new GoogleJsonEngine();

	/**
	 * Constructor.
	 * @param endPointIntrospectorPlugin EndPointIntrospector Plugin
	 * @param routesRegisterPlugin Routes register plugin
	 * @param restHandlerPlugins RestHandler plugins
	 */
	@Inject
	public RestManagerImpl(final EndPointIntrospectorPlugin endPointIntrospectorPlugin, final RoutesRegisterPlugin routesRegisterPlugin, final List<RestHandlerPlugin> restHandlerPlugins) {
		Assertion.checkNotNull(endPointIntrospectorPlugin);
		Assertion.checkNotNull(routesRegisterPlugin);
		Assertion.checkArgument(!restHandlerPlugins.isEmpty(), "No RestHandlerPlugins found, check you have declared your RestHandlerPlugins in RestManagerImpl.\n{0}", STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG);
		Assertion.checkArgument(restHandlerPlugins.get(restHandlerPlugins.size() - 1) instanceof RestfulServiceHandler,
				"RestHandlerPlugins must end with a RestfulServiceHandler in order to dispatsh request to WebService, check your RestHandlerPlugins in RestManagerImpl.\n{0}", STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG);

		//-----
		this.endPointIntrospectorPlugin = endPointIntrospectorPlugin;
		this.routesRegisterPlugin = routesRegisterPlugin;
		handlerChain = new HandlerChain(restHandlerPlugins);
		Home.getDefinitionSpace().register(EndPointDefinition.class);
	}

	/**
	 * Scan and register ResfulServices as EndPointDefinitions.
	 */
	@Override
	public void scanAndRegisterRestfulServices() {
		final List<EndPointDefinition> allEndPointDefinitions = new ArrayList<>();

		//1- We introspect all RestfulService class
		for (final String componentId : Home.getComponentSpace().keySet()) {
			final Object component = Home.getComponentSpace().resolve(componentId, Object.class);
			if (component instanceof RestfulService) {
				final List<EndPointDefinition> endPointDefinitions = endPointIntrospectorPlugin.instrospectEndPoint(((RestfulService) component).getClass());
				allEndPointDefinitions.addAll(endPointDefinitions);
			}
		}

		//2- We sort by path, parameterized path should be after strict path
		Collections.sort(allEndPointDefinitions, new Comparator<EndPointDefinition>() {

			/** {@inheritDoc} */
			@Override
			public int compare(final EndPointDefinition endPointDefinition1, final EndPointDefinition endPointDefinition2) {
				return endPointDefinition1.getPath().compareTo(endPointDefinition2.getPath());
			}

		});

		//3- We register EndPoint Definition in this order
		for (final EndPointDefinition endPointDefinition : allEndPointDefinitions) {
			Home.getDefinitionSpace().put(endPointDefinition, EndPointDefinition.class);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void createAndRegisterWsRestRoute(final EndPointDefinition endPointDefinition) {
		//1- register handlerChain for this endPointDefinition
		routesRegisterPlugin.register(handlerChain, endPointDefinition);
	}

}
