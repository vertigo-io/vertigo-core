/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.core.component.Activeable;
import io.vertigo.lang.WrappedException;
import io.vertigo.vega.impl.webservice.filter.JettyMultipartCleaner;
import io.vertigo.vega.impl.webservice.filter.JettyMultipartConfig;
import spark.Spark;

/**
 * RoutesRegisterPlugin use to register Spark-java route.
 * @author npiedeloup
 */
public final class SparkJavaEmbeddedWebServerPlugin extends AbstractSparkJavaWebServerPlugin implements Activeable {

	/**
	 * @param apiPrefix globale api prefix
	 * @param port Server port
	 */
	@Inject
	public SparkJavaEmbeddedWebServerPlugin(
			@Named("apiPrefix") final Optional<String> apiPrefix,
			@Named("port") final int port) {
		super(apiPrefix);
		Spark.port(port);
		//---
		final String tempDir = System.getProperty("java.io.tmpdir");
		Spark.before(new JettyMultipartConfig(tempDir));
		Spark.after(new JettyMultipartCleaner());
	}

	@Override
	public void start() {
		// nothing

	}

	@Override
	public void stop() {
		Spark.stop();
		// we need to sleep because spark starts a new thread to stop the server
		try {
			Thread.sleep(100L);
		} catch (final InterruptedException e) {
			throw WrappedException.wrap(e);
		}

	}

}
