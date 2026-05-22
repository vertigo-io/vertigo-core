package io.vertigo.arch;

/**
 * An external library or pseudo-package group identified by a single ArchUnit package selector.
 *
 * <pre>{@code
 * static final ArchLib JACKSON = new ArchLib("jackson", "com.fasterxml.jackson..");
 * static final ArchLib JAVA = new ArchLib("java", "java..");
 * }</pre>
 */
record ArchLib(String name, String packagePattern) implements ArchDep {

	@Override
	public String[] packagePatterns() {
		return new String[] { packagePattern };
	}
}
