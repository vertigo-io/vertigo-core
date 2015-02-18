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

import io.vertigo.vega.impl.rest.RoutesRegisterPlugin;
import io.vertigo.vega.plugins.rest.handler.HandlerChain;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import spark.Spark;

/**
 * RoutesRegisterPlugin use to register Spark-java route.
 * @author npiedeloup
 */
public final class SparkJavaRoutesRegisterPlugin implements RoutesRegisterPlugin {

	private static final String defaultContentCharset = "UTF-8";

	/** {@inheritDoc} */
	@Override
	public void register(final HandlerChain handlerChain, final EndPointDefinition endPointDefinition) {

		final WsRestRoute wsRestRoute = new WsRestRoute(endPointDefinition, handlerChain, defaultContentCharset);

		switch (endPointDefinition.getVerb()) {
			case GET:
				Spark.get(wsRestRoute);
				break;
			case POST:
				Spark.post(wsRestRoute);
				break;
			case PUT:
				Spark.put(wsRestRoute);
				break;
			case DELETE:
				Spark.delete(wsRestRoute);
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}
}
