package io.vertigo.core.plugins.node.status.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import io.vertigo.core.node.Node;
import io.vertigo.core.node.NodeInfosPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Plugin to retrieve infos of a node with the http protocol (Rest Webservices)
 * @author mlaroche
 *
 */
public final class HttpNodeInfosPlugin implements NodeInfosPlugin {

	@Override
	public String getConfig(final Node app) {
		return callRestWS(app.getEndPoint() + "/vertigo/components", String.class);
	}

	@Override
	public String getStatus(final Node app) {
		return "";
	}

	@Override
	public Map<String, Object> getStats(final Node app) {
		return Collections.emptyMap();
	}

	@Override
	public String getProtocol() {
		return "http";
	}

	private <R> R callRestWS(final String wsUrl, final Class<? extends R> returnClass) {
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

			//return jsonEngine.fromJson(result.toString("UTF-8"), returnClass);
			return (R) result.toString("UTF-8");
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

	}

}
