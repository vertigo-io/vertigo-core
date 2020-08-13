package io.vertigo.core.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.vertigo.core.lang.Assertion;

public final class FileUtil {
	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private FileUtil() {
		//rien
	}

	public static String read(final URL url) throws IOException, URISyntaxException {
		Assertion.check().isNotNull(url);
		//---
		return Files.readString(Paths.get(url.toURI()));
	}
}
