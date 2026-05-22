package io.vertigo.arch;

import io.vertigo.core.lang.Assertion;

/**
 * An external library or pseudo-package group identified by a single ArchUnit package selector.
 *
 * <pre>{@code
 * static final ArchLib JACKSON = new ArchLib("jackson", "com.fasterxml.jackson..");
 * static final ArchLib JAVA = new ArchLib("java", "java..");
 * }</pre>
 */
record ArchLib(String name, String packagePattern) implements ArchDep {

	ArchLib {
		Assertion.check()
				.isNotBlank(name, "name is required")
				.isNotBlank(packagePattern, "packagePattern is required");
	}

	@Override
	public String[] packagePatterns() {
		return new String[] { packagePattern };
	}
}
