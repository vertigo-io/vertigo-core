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
package io.vertigo.vega.rest;

import io.vertigo.core.Home.App;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.impl.rest.filter.JettyMultipartConfig;
import io.vertigo.vega.plugins.rest.routesregister.sparkjava.SparkJavaRoutesRegister;

import java.util.Arrays;
import java.util.Iterator;

import spark.Spark;

/**
 * Main WebService Route handler.
 * TODO : make configurable
 * @author npiedeloup
 */
public final class WsRestHandler {

	public static final class DtDefinitions implements Iterable<Class<?>> {
		@Override
		public Iterator<Class<?>> iterator() {
			return Arrays.asList(new Class<?>[] {
					Contact.class, ContactCriteria.class
			}).iterator();
		}
	}

	public static void main(final String[] args) {
		Spark.setPort(8088);
		// Création de l'état de l'application
		// Initialisation de l'état de l'application
		final App app = new App(MyApp.config());
		Runtime.getRuntime().addShutdownHook(new OnCloseThread(app));

		// Will serve all static file are under "/public" in classpath if the route isn't consumed by others routes.
		// When using Maven, the "/public" folder is assumed to be in "/main/resources"
		//Spark.externalStaticFileLocation("D:/@GitHub/vertigo/vertigo-vega-impl/src/test/resources/");
		//Spark.before(new IE8CompatibilityFix("8"));
		//Spark.before(new CorsAllower());
		//Translate EndPoint to route
		final String tempDir = System.getProperty("java.io.tmpdir");
		Spark.before(new JettyMultipartConfig(tempDir));
		//Spark.before(new VegaMultipartConfig(tempDir));
		new SparkJavaRoutesRegister().init();
	}

	private static class OnCloseThread extends Thread {
		private final AutoCloseable closeableResource;

		public OnCloseThread(final AutoCloseable closeableResource) {
			super("Vertigo OnCloseThread");
			Assertion.checkNotNull(closeableResource);
			//-----
			this.closeableResource = closeableResource;
		}

		@Override
		public void run() {
			try {
				System.out.println("Try to close " + closeableResource);
				closeableResource.close();
			} catch (final Exception e) {
				System.err.println("Can't close " + closeableResource + " : " + e.toString());
				e.printStackTrace(System.err);
			}
		}
	}
}
