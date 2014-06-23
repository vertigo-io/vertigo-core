package io.vertigo.kernel.resource;

import io.vertigo.kernel.di.configurator.ResourceConfig;

import java.util.List;
import java.util.Set;

/**
 * This object can parse and load resources from a certain type.
 * All 'static' definitions should use this way to be populated.
 *
 * @author pchretien
 */
public interface ResourceLoader {
	/**
	 * @return Types that can be parsed.
	 */
	Set<String> getTypes();

	/**
	 * 
	 * @param List of resources (must be in a type managed by this loader) 
	 */
	void parse(List<ResourceConfig> resourceConfigs);
}
