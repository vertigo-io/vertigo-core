package io.vertigo.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class FileUtil {
	public static String parse(final URL url) throws IOException {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		}
		//		private static String readFile(final URL url) throws IOException, URISyntaxException {
		//			return Files.readString(Paths.get(url.toURI()));
		//		}
	}
}
