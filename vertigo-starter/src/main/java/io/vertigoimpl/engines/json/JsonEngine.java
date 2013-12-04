package io.vertigoimpl.engines.json;

import io.vertigo.kernel.Engine;

public interface JsonEngine extends Engine {

	String toJson(Object data);
}
