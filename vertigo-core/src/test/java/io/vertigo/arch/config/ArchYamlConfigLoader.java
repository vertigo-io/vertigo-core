package io.vertigo.arch.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.vertigo.core.lang.Assertion;

public class ArchYamlConfigLoader {
	private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

	public static ArchConfig parseYaml(final File file) {
		Assertion.check()
				.isNotNull(file, "file is required")
				.isTrue(file.exists(), "YAML file not found: {0}", file.getAbsolutePath());
		//---
		try (InputStream in = new FileInputStream(file)) {
			return YAML.readValue(in, ArchConfig.class);
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot load " + file, e);
		}
	}
}
