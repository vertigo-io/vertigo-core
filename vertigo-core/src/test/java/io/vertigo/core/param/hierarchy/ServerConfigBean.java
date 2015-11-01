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
package io.vertigo.core.param.hierarchy;

/**
 * @author npiedeloup
 */
public final class ServerConfigBean {
	private String name;
	private int port;
	private String host;
	private boolean active;

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public boolean isActive() {
		return active;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

}
