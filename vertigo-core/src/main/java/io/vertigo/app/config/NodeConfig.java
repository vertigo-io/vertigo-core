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
package io.vertigo.app.config;

import java.util.Optional;

import io.vertigo.lang.Assertion;

/**
 * The NodeConfig class defines the config of a node of an entire app.
 *
 * NodeConfig must be created using the NodeConfigBuilder.
 * @author mlaroche
 */
public final class NodeConfig {
	private final String appName;
	private final String nodeId;
	private final Optional<String> endPointOpt;

	NodeConfig(
			final String appName,
			final String nodeId,
			final Optional<String> endPointOpt) {
		Assertion.checkArgNotEmpty(appName);
		Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkNotNull(endPointOpt);
		//---
		this.appName = appName;
		this.nodeId = nodeId;
		this.endPointOpt = endPointOpt;
	}

	/**
	 * Static method factory for AppConfigBuilder
	 * @return NodeConfigBuilder
	 */
	public static NodeConfigBuilder builder() {
		return new NodeConfigBuilder();
	}

	/**
	 * An app is composed of multiple nodes.
	 * AppName is the common name that define the application as a whole. (ex: Facebook, Pharos...)
	 * @return the logical name of the app
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * An app is composed of multiple nodes.
	 * @return the random uuid of a node
	 */
	public String getNodeId() {
		return nodeId;
	}

	public Optional<String> getEndPoint() {
		return endPointOpt;
	}

}
