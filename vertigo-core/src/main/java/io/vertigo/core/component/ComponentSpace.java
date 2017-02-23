package io.vertigo.core.component;

import io.vertigo.lang.Container;
import io.vertigo.util.StringUtil;

public interface ComponentSpace extends Container {
	/**
	 * Resolve a component from its class.
	 * @param componentClass Type of the component
	 * @return Component
	 */
	default <C> C resolve(final Class<C> componentClass) {
		final String normalizedId = StringUtil.first2LowerCase(componentClass.getSimpleName());
		return resolve(normalizedId, componentClass);
	}
}
