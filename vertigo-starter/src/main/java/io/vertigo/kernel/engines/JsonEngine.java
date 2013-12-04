package io.vertigo.kernel.engines;

import io.vertigo.kernel.Engine;

/**
 * @author pchretien
 */
public interface JsonEngine extends Engine {

	String toJson(Object data);
}
