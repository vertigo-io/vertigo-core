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

import io.vertigo.lang.Activeable;
import io.vertigo.vega.impl.rest.filter.JettyMultipartConfig;

import javax.inject.Inject;
import javax.inject.Named;

import spark.Spark;

/**
 * RoutesRegisterPlugin use to register Spark-java route.
 * @author npiedeloup
 */
public final class SparkJavaEmbeddedWebServerPlugin extends AbstractSparkJavaWebServerPlugin implements Activeable {

	private final int port;

	/**
	 * @param port Server port
	 */
	@Inject
	public SparkJavaEmbeddedWebServerPlugin(@Named("port") final int port) {
		this.port = port;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		Spark.setPort(port);
		//---
		final String tempDir = System.getProperty("java.io.tmpdir");
		Spark.before(new JettyMultipartConfig(tempDir));
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//nothing
	}
}
