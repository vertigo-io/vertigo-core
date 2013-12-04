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

import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigoimpl.engines.json.GoogleJsonEngine;
import io.vertigoimpl.engines.json.JsonEngine;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.NotFoundException;

@Path("/spaces")
@Produces("application/json")
public class ComponentCmd {
	private static final JsonEngine jsonAdapter = new GoogleJsonEngine();

	@Path("/components")
	@GET
	public String getComponentSpaceConfig() {
		return jsonAdapter.toJson(Home.getComponentSpace().getConfig());
	}

	@Path("/components/{componentId}")
	@GET
	public String getComponentConfig(@PathParam("componentId") String componentId) {
		Assertion.checkArgNotEmpty(componentId);
		//---------------------------------------------------------------------
		JsonArray jsonModuleConfigs = doGetModuleConfigs();
		for (int i = 0; i < jsonModuleConfigs.size(); i++) {
			JsonObject jsonModuleConfig = (JsonObject) jsonModuleConfigs.get(i);
			JsonArray jsonComponentConfigs = jsonModuleConfig.get("componentConfigs").getAsJsonArray();

			for (int j = 0; j < jsonComponentConfigs.size(); j++) {
				JsonObject jsonComponentConfig = (JsonObject) jsonComponentConfigs.get(j);
				if (componentId.equalsIgnoreCase(jsonComponentConfig.get("id").getAsString())) {
					return jsonAdapter.toJson(jsonComponentConfig);
				}
			}
		}
		throw new NotFoundException();
	}

	@Path("/components/modules")
	@GET
	public String getModuleConfigs() {
		return jsonAdapter.toJson(doGetModuleConfigs());
	}

	private JsonArray doGetModuleConfigs() {
		String json = getComponentSpaceConfig();
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = (JsonObject) parser.parse(json);
		JsonArray jsonModuleConfigs = jsonObject.get("moduleConfigs").getAsJsonArray();
		return jsonModuleConfigs;
	}

	@Path("components/modules/{moduleName}")
	@GET
	public String getModuleConfig(@PathParam("moduleName") String moduleName) {
		Assertion.checkArgNotEmpty(moduleName);
		//---------------------------------------------------------------------
		JsonArray jsonModuleConfigs = doGetModuleConfigs();
		for (int i = 0; i < jsonModuleConfigs.size(); i++) {
			JsonObject jsonModuleConfig = (JsonObject) jsonModuleConfigs.get(i);
			if (moduleName.equalsIgnoreCase(jsonModuleConfig.get("name").getAsString())) {
				return jsonAdapter.toJson(jsonModuleConfig);
			}
		}
		throw new NotFoundException();
	}

	@Path("/definitions")
	@GET
	public String getDefinitionSpace() {
		return jsonAdapter.toJson(Home.getDefinitionSpace());
	}

	@Path("/definitions/types")
	@GET
	public String getDefinitionTypes() {
		return jsonAdapter.toJson(Home.getDefinitionSpace().getAllTypes());
	}

	@Path("/definitions/types/{definitionType}")
	@GET
	public String getDefinitionType(@PathParam("definitionType") String definitionType) {
		for (Class<? extends Definition> definitionClass : Home.getDefinitionSpace().getAllTypes()) {
			if (definitionClass.getSimpleName().equals(definitionType)) {
				return jsonAdapter.toJson(Home.getDefinitionSpace().getAll(definitionClass));
			}
		}
		throw new NotFoundException();
	}

	@Path("/definitions/{definitionName}")
	@GET
	public String getDefinition(@PathParam("definitionName") String definitionName) {
		return jsonAdapter.toJson(Home.getDefinitionSpace().resolve(definitionName));
	}
}
