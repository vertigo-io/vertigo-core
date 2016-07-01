/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.vega.plugins.webservice.webserver.sparkjava;

import java.util.List;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.vega.impl.webservice.WebServerPlugin;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import spark.Spark;

/**
 * RoutesRegisterPlugin use to register Spark-java route.
 * @author npiedeloup
 */
abstract class AbstractSparkJavaWebServerPlugin implements WebServerPlugin {
	private static final String DEFAULT_CONTENT_CHARSET = "UTF-8";
	private final Option<String> apiPrefix;

	public AbstractSparkJavaWebServerPlugin(final Option<String> apiPrefix) {
		Assertion.checkNotNull(apiPrefix);
		Assertion.checkArgument(!apiPrefix.isPresent() || apiPrefix.get().startsWith("/"), "Global route apiPrefix must starts with /");
		//-----
		this.apiPrefix = apiPrefix;
	}

	/** {@inheritDoc} */
	@Override
	public final void registerWebServiceRoute(final HandlerChain handlerChain, final List<WebServiceDefinition> webServiceDefinitions) {
		Assertion.checkNotNull(handlerChain);
		Assertion.checkNotNull(webServiceDefinitions);
		//-----
		boolean corsProtected = false;
		for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
			final SparkJavaRoute sparkJavaRoute = new SparkJavaRoute(apiPrefix, webServiceDefinition, handlerChain, DEFAULT_CONTENT_CHARSET);
			switch (webServiceDefinition.getVerb()) {
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
			corsProtected = corsProtected || webServiceDefinition.isCorsProtected();

		}
		if (corsProtected) {
			final SparkJavaOptionsRoute sparkJavaOptionsRoute = new SparkJavaOptionsRoute(handlerChain);
			Spark.options(sparkJavaOptionsRoute);
		}

	}
}
