/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.node.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.param.Param;

/**
 * The connectorConfigBuilder defines the configuration of a connector.
 * A connector is a bridge to an external component/library
  *
 * @author mlaroche
 */
public final class ConnectorConfigBuilder implements Builder<ConnectorConfig> {
	private final Class<? extends Connector> myConnectorImplClass;
	private final List<Param> myParams = new ArrayList<>();

	/**
	 * Constructor.
	 * @param connectorImplClass impl of the connector
	 */
	ConnectorConfigBuilder(final Class<? extends Connector> connectorImplClass) {
		Assertion.checkNotNull(connectorImplClass);
		//-----
		myConnectorImplClass = connectorImplClass;
	}

	/**
	 * Adds a param to this plugin.
	 * @param params the list of params
	 * @return this builder
	 */
	public ConnectorConfigBuilder addAllParams(final Param... params) {
		Assertion.checkNotNull(params);
		//-----
		myParams.addAll(Arrays.asList(params));
		return this;
	}

	/**
	 * Adds a param to this plugin.
	 * @param param the param
	 * @return this builder
	 */
	public ConnectorConfigBuilder addParam(final Param param) {
		Assertion.checkNotNull(param);
		//-----
		myParams.add(param);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ConnectorConfig build() {
		return new ConnectorConfig(
				myConnectorImplClass,
				myParams);
	}
}
