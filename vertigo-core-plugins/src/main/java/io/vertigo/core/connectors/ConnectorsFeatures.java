/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.connectors;

import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ComponentConfigBuilder;
import io.vertigo.app.config.Features;
import io.vertigo.core.connectors.redis.RedisConnector;
import io.vertigo.core.param.Param;

/**
 * Defines basic connectors.
 * @author pchretien
 */
public final class ConnectorsFeatures extends Features {

	/**
	 * Constructor
	 */
	public ConnectorsFeatures() {
		super("connectors");
	}

	/**
	 * Adds a REDIS connector.
	 * @param host the REDIS host
	 * @param port the REDIS port
	 * @param database the index of the REDIS database
	 * @return the REDIS connector
	 */
	public ConnectorsFeatures withRedisConnector(final String host, final int port, final int database) {
		return withRedisConnector(host, port, null, database);
	}

	/**
	 * Adds a REDIS connector.
	 * @param host the REDIS host
	 * @param port the REDIS port
	 * @param password the REDIS password
	 * @param database the index of the REDIS database
	 * @return the REDIS connector
	 */
	public ConnectorsFeatures withRedisConnector(final String host, final int port, final String password, final int database) {
		final ComponentConfigBuilder componentConfigBuilder = ComponentConfig.builder(RedisConnector.class)
				.addParam(Param.of("host", host))
				.addParam(Param.of("port", Integer.toString(port)))
				.addParam(Param.of("database", Integer.toString(database)));
		if (password != null) {
			componentConfigBuilder
					.addParam(Param.of("password", password));
		}
		getModuleConfigBuilder()
				.addComponent(componentConfigBuilder.build());
		return this;

	}

	@Override
	protected void buildFeatures() {
		//
	}

}
