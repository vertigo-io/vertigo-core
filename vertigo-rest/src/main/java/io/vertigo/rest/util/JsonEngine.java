package io.vertigo.rest.util;

import io.vertigo.kernel.Engine;

/**
 * @author pchretien
 */
public interface JsonEngine extends Engine {

	String toJson(Object data);
}
