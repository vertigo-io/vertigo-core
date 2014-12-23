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

import io.vertigo.core.Home;
import io.vertigo.vega.impl.rest.handler.WsRestRoute;
import io.vertigo.vega.rest.RestManager;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;

import java.util.Collection;

import spark.Spark;
import spark.servlet.SparkApplication;

/**
 * Application class, use to register Spark-java route.
 * Could be embedded in Tomcat Server (see http://www.sparkjava.com/readme.html#title19)
 *
 * @author npiedeloup
 */
public final class SparkJavaRoutesRegister implements SparkApplication {

	/**
	 * Spark-java application class.
	 * Translate EndPointDefinitions to Spark routes.
	 */
	@Override
	public void init() {
		final RestManager restManager = Home.getComponentSpace().resolve(RestManager.class);
		final String defaultContentCharset = "UTF-8"; //TODO : parametrable ?
		restManager.scanAndRegisterRestfulServices();

		//Translate EndPoint to route
		final Collection<EndPointDefinition> endPointDefinitions = Home.getDefinitionSpace().getAll(EndPointDefinition.class);

		for (final EndPointDefinition endPointDefinition : endPointDefinitions) {
			switch (endPointDefinition.getVerb()) {
				case GET:
					Spark.get(new WsRestRoute(endPointDefinition, defaultContentCharset));
					break;
				case POST:
					Spark.post(new WsRestRoute(endPointDefinition, defaultContentCharset));
					break;
				case PUT:
					Spark.put(new WsRestRoute(endPointDefinition, defaultContentCharset));
					break;
				case DELETE:
					Spark.delete(new WsRestRoute(endPointDefinition, defaultContentCharset));
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}
}
