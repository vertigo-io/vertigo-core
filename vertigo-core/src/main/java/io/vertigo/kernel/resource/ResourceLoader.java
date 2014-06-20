package io.vertigo.kernel.resource;

import java.util.Map;

public interface ResourceLoader {
	void add(Map<String, String> resources);

	void solve();
}
