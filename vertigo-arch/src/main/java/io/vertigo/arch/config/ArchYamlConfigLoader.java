package io.vertigo.arch.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** Parses an {@link ArchConfig} from a YAML file. */
public final class ArchYamlConfigLoader {
	private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

	private ArchYamlConfigLoader() {
	}

	public static ArchConfig parseYaml(final File file) {
		Objects.requireNonNull(file, "file is required");
		if (!file.exists()) {
			throw new IllegalArgumentException("YAML file not found: " + file.getAbsolutePath());
		}
		//---
		try (InputStream in = new FileInputStream(file)) {
			return YAML.readValue(in, ArchConfig.class);
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot load " + file, e);
		}
	}
}
