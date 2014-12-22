package io.vertigo.core.config;

import io.vertigo.lang.Assertion;

/**
 * LogConfile is the unique point used to configure Log.
 *
 * @author pchretien
 */
public final class LogConfig {
	private final String fileName;

	public LogConfig(final String fileName) {
		Assertion.checkArgNotEmpty(fileName);
		//-----
		this.fileName = fileName;

	}

	public String getFileName() {
		return fileName;
	}
}
