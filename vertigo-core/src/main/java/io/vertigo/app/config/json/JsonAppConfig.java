package io.vertigo.app.config.json;

import java.util.HashMap;
import java.util.List;

import io.vertigo.app.config.json.JsonAppConfig.JsonModuleConfig;

public final class JsonAppConfig extends HashMap<String, JsonModuleConfig> {

	private static final long serialVersionUID = 8170148586508859017L;

	public static class JsonModuleConfig {
		public JsonFeaturesConfig features;
		public List<JsonPluginConfig> plugins;
	}

	public static class JsonFeaturesConfig extends HashMap<String, JsonParamsConfig> {
		private static final long serialVersionUID = 1L;
		//nothing more
	}

	public static class JsonPluginConfig {
		public String className;
		public JsonParamsConfig params;
	}

	public static class JsonParamsConfig extends HashMap<String, String> {
		private static final long serialVersionUID = 1L;
		// nothing more
	}

}
