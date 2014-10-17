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
import io.vertigo.vega.rest.EndPointIntrospectorPlugin;
import io.vertigo.vega.rest.RestManager;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

/**
 * Restful webservice manager.
 * @author npiedeloup
 */
public final class RestManagerImpl implements RestManager {
	private final EndPointIntrospectorPlugin endPointIntrospectorPlugin;

	/**
	 * Constructor.
	 * @param endPointIntrospectorPlugin EndPointIntrospector Plugin
	 */
	@Inject
	public RestManagerImpl(final EndPointIntrospectorPlugin endPointIntrospectorPlugin) {
		Assertion.checkNotNull(endPointIntrospectorPlugin);
		//---------------------------------------------------------------------
		this.endPointIntrospectorPlugin = endPointIntrospectorPlugin;
		Home.getDefinitionSpace().register(EndPointDefinition.class);
	}

	/**
	 * Scan and register ResfulServices as EndPointDefinitions.
	 */
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

			public int compare(final EndPointDefinition endPointDefinition1, final EndPointDefinition endPointDefinition2) {
				return endPointDefinition1.getPath().compareTo(endPointDefinition2.getPath());
			}

		});

		//3- We register EndPoint Definition in this order
		for (final EndPointDefinition endPointDefinition : allEndPointDefinitions) {
			Home.getDefinitionSpace().put(endPointDefinition, EndPointDefinition.class);
		}
	}

}
