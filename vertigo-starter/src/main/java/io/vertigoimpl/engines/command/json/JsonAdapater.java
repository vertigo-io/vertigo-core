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
package io.vertigoimpl.engines.command.json;

import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.lang.JsonExclude;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public final class JsonAdapater {
	private final Gson gson = createGson();

	public <R> String toJson(R data) {
		return gson.toJson(data);
	}

	private static Gson createGson() {
		return new GsonBuilder()//
				//.setPrettyPrinting()//
				//.serializeNulls()//On veut voir les null
				.registerTypeAdapter(ComponentInfo.class, new JsonSerializer<ComponentInfo>() {
					@Override
					public JsonElement serialize(ComponentInfo componentInfo, Type typeOfSrc, JsonSerializationContext context) {
						JsonObject jsonObject = new JsonObject();
						jsonObject.add(componentInfo.getTitle(), context.serialize(componentInfo.getValue()));
						return jsonObject;
					}
				})//	
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
}
