package io.vertigo.kernel.di.configurator;

import io.vertigo.kernel.lang.Assertion;

/**
 * A resource is defined by 
 * - a type 
 * - a path
 * A resource can be a file, a blob or a simple java class.
 * A resource is used to configure a module.  
 * 
 * @author pchretien
 */
public final class ResourceConfig {
	private final String type;
	private final String path;

	ResourceConfig(String type, String path) {
		Assertion.checkArgNotEmpty(type);
		Assertion.checkArgNotEmpty(path);
		//---------------------------------------------------------------------
		this.type = type;
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "{ type: " + type + ", path: " + path + " }";
	}
}
