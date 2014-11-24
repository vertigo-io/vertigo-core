package io.vertigo.core.config;

import io.vertigo.core.engines.AopEngine;
import io.vertigo.core.engines.ElasticaEngine;
import io.vertigo.core.engines.VCommandEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * @author pchretien
 */
public final class AppConfig {
	private final Map<String, String> params;
	private final Properties envParams;

	private final List<ModuleConfig> modules;
	//---
	private final boolean silence;
	@JsonExclude
	private final AopEngine aopEngine;
	@JsonExclude
	private final Option<ElasticaEngine> elasticaEngine;
	@JsonExclude
	private final Option<VCommandEngine> commandEngine;

	AppConfig(final Properties envParams, final Map<String, String> params, final List<ModuleConfig> moduleConfigs, final AopEngine aopEngine, final Option<ElasticaEngine> elasticaEngine, final Option<VCommandEngine> commandEngine, final boolean silence) {
		Assertion.checkNotNull(envParams);
		Assertion.checkNotNull(params);
		Assertion.checkNotNull(moduleConfigs);
		//---
		Assertion.checkNotNull(aopEngine);
		Assertion.checkNotNull(elasticaEngine);
		Assertion.checkNotNull(commandEngine);
		//---------------------------------------------------------------------
		this.params = params;
		this.envParams = envParams;
		this.modules = Collections.unmodifiableList(new ArrayList<>(moduleConfigs));
		//---
		this.silence = silence;
		this.aopEngine = aopEngine;
		this.elasticaEngine = elasticaEngine;
		this.commandEngine = commandEngine;
	}

	public Properties getEnvParams() {
		return envParams;
	}

	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * @return Liste des configurations de modules
	 */
	public List<ModuleConfig> getModuleConfigs() {
		return modules;
	}

	public boolean isSilence() {
		return silence;
	}

	public AopEngine getAopEngine() {
		return aopEngine;
	}

	public Option<VCommandEngine> getCommandEngine() {
		return commandEngine;
	}

	public Option<ElasticaEngine> getElasticaEngine() {
		return elasticaEngine;
	}
}
