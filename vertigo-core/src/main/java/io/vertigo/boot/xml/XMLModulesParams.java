package io.vertigo.boot.xml;

import io.vertigo.lang.Assertion;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author pchretien
 */
final class XMLModulesParams {
	private final Properties properties;
	private final Set<String> keys;
	private final Set<String> readKeys = new HashSet<>();

	XMLModulesParams(final Properties properties) {
		Assertion.checkNotNull(properties);
		//-----
		this.properties = properties;
		keys = properties.stringPropertyNames();
	}

	String getParam(final String paramName) {
		Assertion.checkArgNotEmpty(paramName);
		Assertion.checkArgument(properties.containsKey(paramName), "property '{0}' not found", paramName);
		//-----
		readKeys.add(paramName);
		return properties.getProperty(paramName);
	}

	Set<String> unreadProperties() {
		keys.removeAll(readKeys);
		return keys;
	}
}
