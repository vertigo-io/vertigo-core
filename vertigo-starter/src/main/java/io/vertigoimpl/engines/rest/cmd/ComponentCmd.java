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
import io.vertigo.kernel.lang.JsonExclude;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sun.jersey.api.NotFoundException;

@Path("/spaces")
@Produces("application/json")
public class ComponentCmd {
	private final Gson gson = createGson();

	private static Gson createGson() {
		return new GsonBuilder()//
				.setPrettyPrinting()//
				//.serializeNulls()//On veut voir les null
				.registerTypeAdapter(List.class, new JsonSerializer<List>() {

					@Override
					public JsonElement serialize(List src, Type typeOfSrc, JsonSerializationContext context) {
						if (src.isEmpty()) {
							return null;
						}
						return context.serialize(src);
					}
				})//	
				.registerTypeAdapter(Map.class, new JsonSerializer<Map>() {

					@Override
					public JsonElement serialize(Map src, Type typeOfSrc, JsonSerializationContext context) {
						if (src.isEmpty()) {
							return null;
						}
						return context.serialize(src);
					}
				})//
				.registerTypeAdapter(DefinitionReference.class, new JsonSerializer<DefinitionReference>() {

					@Override
					public JsonElement serialize(DefinitionReference src, Type typeOfSrc, JsonSerializationContext context) {
						return context.serialize(src.get().getName());
					}
				})//
				.registerTypeAdapter(Option.class, new JsonSerializer<Option>() {

					@Override
					public JsonElement serialize(Option src, Type typeOfSrc, JsonSerializationContext context) {
						if (src.isDefined()) {
							return context.serialize(src.get());
						}
						return null; //rien
					}
				})//			
				.registerTypeAdapter(Class.class, new JsonSerializer<Class>() {

					@Override
					public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
						return new JsonPrimitive(src.getName());
					}
				})//
				.addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes arg0) {
						if (arg0.getAnnotation(JsonExclude.class) != null) {
							return true;
						}
						return false;
					}

					@Override
					public boolean shouldSkipClass(Class<?> arg0) {
						return false;
					}
				}).create();
	}

	@Path("/components")
	@GET
	public String getComponentSpaceConfig() {
		return gson.toJson(Home.getComponentSpace().getConfig());
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
					return gson.toJson(jsonComponentConfig);
				}
			}
		}
		throw new NotFoundException();
	}

	@Path("/components/modules")
	@GET
	public String getModuleConfigs() {
		return gson.toJson(doGetModuleConfigs());
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
				return gson.toJson(jsonModuleConfig);
			}
		}
		throw new NotFoundException();
	}

	@Path("/definitions")
	@GET
	public String getDefinitionSpace() {
		return gson.toJson(Home.getDefinitionSpace());
	}

	@Path("/definitions/types")
	@GET
	public String getDefinitionTypes() {
		return gson.toJson(Home.getDefinitionSpace().getAllTypes());
	}

	@Path("/definitions/types/{definitionType}")
	@GET
	public String getDefinitionType(@PathParam("definitionType") String definitionType) {
		for (Class<? extends Definition> definitionClass : Home.getDefinitionSpace().getAllTypes()) {
			if (definitionClass.getSimpleName().equals(definitionType)) {
				return gson.toJson(Home.getDefinitionSpace().getAll(definitionClass));
			}
		}
		throw new NotFoundException();
	}

	@Path("/definitions/{definitionName}")
	@GET
	public String getDefinition(@PathParam("definitionName") String definitionName) {
		return gson.toJson(Home.getDefinitionSpace().resolve(definitionName));
	}
}
