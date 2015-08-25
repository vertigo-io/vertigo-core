package io.vertigo.commons.impl.daemon;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Super Conteneur.
 *
 * @author npiedeloup
 */
final class DualContainer implements Container {
	private final Container container;
	private final Map<String, Object> params;
	private final Set<String> ids;
	private final Set<String> unusedKeys;

	DualContainer(final Container container, final Map<String, Object> params) {
		Assertion.checkNotNull(container);
		Assertion.checkNotNull(params);
		//-----
		this.container = container;
		this.params = params;
		ids = new LinkedHashSet<>();
		ids.addAll(container.keySet());
		ids.addAll(params.keySet());
		Assertion.checkArgument(ids.size() == container.keySet().size() + params.keySet().size(), "Ambiguité : il y a des ids en doublon");
		unusedKeys = new HashSet<>(ids);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.checkNotNull(id);
		//-----
		return ids.contains(id);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O resolve(final String id, final Class<O> clazz) {
		Assertion.checkNotNull(id);
		Assertion.checkNotNull(clazz);
		//-----
		unusedKeys.remove(id);
		if (container.contains(id)) {
			return container.resolve(id, clazz);
		}
		if (params.containsKey(id)) {
			return clazz.cast(params.get(id));
		}
		throw new RuntimeException("component info with id '" + id + "' not found.");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return ids;
	}

	Set<String> getUnusedKeys() {
		return unusedKeys;
	}
}
