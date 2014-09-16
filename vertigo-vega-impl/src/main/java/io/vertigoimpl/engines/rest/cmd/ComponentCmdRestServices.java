/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigoimpl.engines.rest.cmd;

import io.vertigo.core.Home;
import io.vertigo.core.di.configurator.ComponentSpaceConfig;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.metamodel.Definition;
import io.vertigo.core.metamodel.DefinitionSpace;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.engine.GoogleJsonEngine;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.rest.stereotype.GET;
import io.vertigo.vega.rest.stereotype.PathParam;

import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ComponentCmdRestServices implements RestfulService {
	private final JsonEngine jsonEngine = new GoogleJsonEngine();

	@AnonymousAccessAllowed
	@GET("/vertigo/components")
	public ComponentSpaceConfig getComponentSpaceConfig() {
		return Home.getComponentSpace().getConfig();
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/components/{componentId}")
	public String getComponentConfig(@PathParam("componentId") final String componentId) {
		Assertion.checkArgNotEmpty(componentId);
		//---------------------------------------------------------------------
		final JsonArray jsonModuleConfigs = doGetModuleConfigs();
		for (int i = 0; i < jsonModuleConfigs.size(); i++) {
			final JsonObject jsonModuleConfig = (JsonObject) jsonModuleConfigs.get(i);
			final JsonArray jsonComponentConfigs = jsonModuleConfig.get("componentConfigs").getAsJsonArray();

			for (int j = 0; j < jsonComponentConfigs.size(); j++) {
				final JsonObject jsonComponentConfig = (JsonObject) jsonComponentConfigs.get(j);
				if (componentId.equalsIgnoreCase(jsonComponentConfig.get("id").getAsString())) {
					return jsonEngine.toJson(jsonComponentConfig);
				}
			}
		}
		throw new RuntimeException("NotFoundException");
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/components/modules")
	public String getModuleConfigs() {
		return jsonEngine.toJson(doGetModuleConfigs());
	}

	private JsonArray doGetModuleConfigs() {
		final String json = jsonEngine.toJson(Home.getComponentSpace().getConfig());
		final JsonParser parser = new JsonParser();
		final JsonObject jsonObject = (JsonObject) parser.parse(json);
		final JsonArray jsonModuleConfigs = jsonObject.get("moduleConfigs").getAsJsonArray();
		return jsonModuleConfigs;
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/components/modules/{moduleName}")
	public String getModuleConfig(@PathParam("moduleName") final String moduleName) {
		Assertion.checkArgNotEmpty(moduleName);
		//---------------------------------------------------------------------
		final JsonArray jsonModuleConfigs = doGetModuleConfigs();
		for (int i = 0; i < jsonModuleConfigs.size(); i++) {
			final JsonObject jsonModuleConfig = (JsonObject) jsonModuleConfigs.get(i);
			if (moduleName.equalsIgnoreCase(jsonModuleConfig.get("name").getAsString())) {
				return jsonEngine.toJson(jsonModuleConfig);
			}
		}
		throw new RuntimeException("NotFoundException");
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/definitions")
	public DefinitionSpace getDefinitionsSpace() {
		return Home.getDefinitionSpace();
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/types")
	public Collection<Class<? extends Definition>> getDefinitionTypes() {
		return Home.getDefinitionSpace().getAllTypes();
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/definitions/types/{definitionType}")
	public String getDefinitionType(@PathParam("definitionType") final String definitionType) {
		for (final Class<? extends Definition> definitionClass : Home.getDefinitionSpace().getAllTypes()) {
			if (definitionClass.getSimpleName().equals(definitionType)) {
				return jsonEngine.toJson(Home.getDefinitionSpace().getAll(definitionClass));
			}
		}
		throw new RuntimeException("NotFoundException");
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/definitions/{definitionName}")
	public Definition getDefinition(@PathParam("definitionName") final String definitionName) {
		return Home.getDefinitionSpace().resolve(definitionName);
	}
}
