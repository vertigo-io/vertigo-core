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
package io.vertigo.vega.plugins.rest.webserver.sparkjava;

import io.vertigo.lang.Assertion;
import io.vertigo.vega.impl.rest.WebServerPlugin;
import io.vertigo.vega.plugins.rest.handler.HandlerChain;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;

import java.util.List;

import spark.Spark;

/**
 * RoutesRegisterPlugin use to register Spark-java route.
 * @author npiedeloup
 */
abstract class AbstractSparkJavaWebServerPlugin implements WebServerPlugin {
	private static final String DEFAULT_CONTENT_CHARSET = "UTF-8";

	/** {@inheritDoc} */
	@Override
	public final void registerWsRoute(final HandlerChain handlerChain, final List<EndPointDefinition> endPointDefinitions) {
		Assertion.checkNotNull(handlerChain);
		Assertion.checkNotNull(endPointDefinitions);
		//-----
		for (final EndPointDefinition endPointDefinition : endPointDefinitions) {
			final SparkJavaRoute sparkJavaRoute = new SparkJavaRoute(endPointDefinition, handlerChain, DEFAULT_CONTENT_CHARSET);
			switch (endPointDefinition.getVerb()) {
				case GET:
					Spark.get(sparkJavaRoute);
					break;
				case POST:
					Spark.post(sparkJavaRoute);
					break;
				case PUT:
					Spark.put(sparkJavaRoute);
					break;
				case PATCH:
					Spark.patch(sparkJavaRoute);
					break;
				case DELETE:
					Spark.delete(sparkJavaRoute);
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}
}
