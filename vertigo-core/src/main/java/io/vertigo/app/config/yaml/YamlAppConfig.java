package io.vertigo.app.config.yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlAppConfig {

	public YamlBootConfig boot;
	public LinkedHashMap<String, YamlModuleConfig> modules = new LinkedHashMap<>();
	public List<YamlInitializerConfig> initializers = new ArrayList<>();

	public static final class YamlBootConfig {
		public YamlParamsConfig params;
		public List<YamlPluginConfig> plugins = new ArrayList<>();

	}

	public static class YamlModuleConfig {
		public List<YamlFeatureConfig> features;
		public List<YamlFeatureConfig> featuresConfig;
		public List<String> __flags__ = new ArrayList<>();
		public List<YamlPluginConfig> plugins = new ArrayList<>();
	}

	public static class YamlFeatureConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = 1L;
		//nothing more
	}

	public static class YamlPluginConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = 1L;
		//nothing more
	}

	public static class YamlParamsConfig extends LinkedHashMap<String, String> {
		private static final long serialVersionUID = 1L;
		// nothing more
	}

	public static class YamlInitializerConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = 1L;
		// nothing more
	}

}
