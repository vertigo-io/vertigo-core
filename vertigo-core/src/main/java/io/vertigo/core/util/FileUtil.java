package io.vertigo.core.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;

public final class FileUtil {
	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private FileUtil() {
		//rien
	}

	public static String read(final URL url) {
		Assertion.check().isNotNull(url);
		//---
		try {
			return Files.readString(Paths.get(url.toURI()));
		} catch (final IOException | URISyntaxException e) {
			throw WrappedException.wrap(e, "Error when reading file : '{0}'", url);
		}
	}
}
