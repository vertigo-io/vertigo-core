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
package io.vertigo.commons.plugins.node.infos.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.vertigo.commons.analytics.health.HealthCheck;
import io.vertigo.commons.impl.node.NodeInfosPlugin;
import io.vertigo.commons.node.Node;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Plugin to retrieve infos of a node with the http protocol (Rest Webservices)
 * @author mlaroche
 *
 */
public final class HttpNodeInfosPlugin implements NodeInfosPlugin {

	private static final Gson GSON = new Gson();

	@Override
	public String getConfig(final Node app) {
		return callRestWS(app.getEndPoint().get() + "/vertigo/components", JsonObject.class).toString();
	}

	@Override
	public List<HealthCheck> getStatus(final Node app) {
		return callRestWS(app.getEndPoint().get() + "/vertigo/healthcheck", new TypeToken<List<HealthCheck>>() {
			/**/}.getType());
	}

	@Override
	public Map<String, Object> getStats(final Node app) {
		return Collections.emptyMap();
	}

	@Override
	public String getProtocol() {
		return "http";
	}

	private static <R> R callRestWS(final String wsUrl, final Type returnType) {
		Assertion.checkArgNotEmpty(wsUrl);
		// ---
		try {
			final URL url = new URL(wsUrl);
			final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(500);
			httpURLConnection.setRequestProperty("Content-Type", "application/json");

			final ByteArrayOutputStream result = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			try (InputStream inputStream = httpURLConnection.getInputStream()) {
				int length;
				while ((length = inputStream.read(buffer)) != -1) {
					result.write(buffer, 0, length);
				}
			}
			return GSON.fromJson(result.toString("UTF-8"), returnType);
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

	}

}
