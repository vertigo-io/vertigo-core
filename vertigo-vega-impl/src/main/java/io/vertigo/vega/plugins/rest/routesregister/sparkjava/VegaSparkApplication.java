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

import spark.servlet.SparkApplication;

/**
 * Application class, use to register Vertigo Service as Spark-java route.
 * Could be embedded in Tomcat Server (see http://www.sparkjava.com/readme.html#title19)
 *
 * @author npiedeloup
 */
public final class VegaSparkApplication implements SparkApplication {
	//
	/**
	 * Spark-java application class.
	 * Translate EndPointDefinitions to Spark routes.
	 */
	@Override
	public void init() {
		//
	}
}
