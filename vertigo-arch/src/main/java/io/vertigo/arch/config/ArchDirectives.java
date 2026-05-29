package io.vertigo.arch.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Optional directives that activate additional architecture checks on a module config.
 * All directives default to {@code false} — absent from YAML means disabled.
 *
 * <pre>
 * directives:
 *   no-cycle: true
 *   no-direct-impl-dep: true
 * </pre>
 */
public record ArchDirectives(
		@JsonProperty("no-cycle") boolean noCycle,
		@JsonProperty("no-direct-impl-dep") boolean noDirectImplDep) {

	/** All directives disabled — used when the YAML omits the directives section. */
	static ArchDirectives none() {
		return new ArchDirectives(false, false);
	}
}
