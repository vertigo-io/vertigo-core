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
package io.vertigo.app.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * The NodeConfigBuilder builder allows you to create an NodeConfig using a fluent, simple style .
 *
 * @author mlaroche
 */
public final class NodeConfigBuilder implements Builder<NodeConfig> {
	private String myAppName;
	private String myNodeId;
	private String myEndPoint;

	public NodeConfigBuilder withAppName(final String appName) {
		Assertion.checkState(myAppName == null, "appName '{0}' is not allowed. appName is already defined as '{1}'", appName, myAppName);
		Assertion.checkArgNotEmpty(appName);
		// ---
		myAppName = appName;
		return this;

	}

	public NodeConfigBuilder withNodeId(final String nodeId) {
		Assertion.checkState(myNodeId == null, "nodeId '{0}' is not allowed. nodeId is already defined as '{1}'", nodeId, myNodeId);
		Assertion.checkArgNotEmpty(nodeId);
		// ---
		myNodeId = nodeId;
		return this;

	}

	public NodeConfigBuilder withEndPoint(final String endPoint) {
		Assertion.checkState(myEndPoint == null, "endPoint '{0}' is not allowed. endPoint is already defined as '{1}'", endPoint, myEndPoint);
		Assertion.checkArgNotEmpty(endPoint);
		// ---
		myEndPoint = endPoint;
		return this;

	}

	/**
	 * Builds the appConfig.
	 * @return appConfig.
	 */
	@Override
	public NodeConfig build() {
		if (myAppName == null) {
			myAppName = "myApp";
		}

		if (myNodeId == null) {
			myNodeId = UUID.randomUUID().toString();
		}
		if (myEndPoint == null) {
			myEndPoint = retrieveHostName();
		}

		return new NodeConfig(
				myAppName,
				myNodeId,
				myEndPoint);
	}

	private static String retrieveHostName() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (final UnknownHostException e) {
			return "UnknownHost";
		}
	}

}
