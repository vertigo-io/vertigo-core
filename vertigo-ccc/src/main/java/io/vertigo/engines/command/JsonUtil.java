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
package io.vertigo.engines.command;

import io.vertigo.core.spaces.component.ComponentInfo;
import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.Option;

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

/**
 * @author pchretien
 */
public final class JsonUtil {

	private static final Gson gson = createGson();

	private JsonUtil() {
		//private
	}

	/**
	 * @param data Object to encode
	 * @return Json string
	 */
	public static String toJson(final Object data) {
		return gson.toJson(data);
	}

	private static Gson createGson() {
		return new GsonBuilder()
				//.setPrettyPrinting()
				//.serializeNulls()//On veut voir les null
				.registerTypeAdapter(ComponentInfo.class, new ComponentInfoJsonSerializer())
				.registerTypeAdapter(List.class, new ListJsonSerializer())
				.registerTypeAdapter(Map.class, new MapJsonSerializer())
				.registerTypeAdapter(DefinitionReference.class, new DefinitionReferenceJsonSerializer())
				.registerTypeAdapter(Option.class, new OptionJsonSerializer())
				.registerTypeAdapter(Class.class, new ClassJsonSerializer())//
				.addSerializationExclusionStrategy(new JsonExclusionStrategy()).create();
	}

	private static final class JsonExclusionStrategy implements ExclusionStrategy {
		@Override
		public boolean shouldSkipField(final FieldAttributes arg0) {
			if (arg0.getAnnotation(JsonExclude.class) != null) {
				return true;
			}
			return false;
		}

		@Override
		public boolean shouldSkipClass(final Class<?> arg0) {
			return false;
		}
	}

	private static final class ClassJsonSerializer implements JsonSerializer<Class> {
		@Override
		public JsonElement serialize(final Class src, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(src.getName());
		}
	}

	private static final class OptionJsonSerializer implements JsonSerializer<Option> {
		@Override
		public JsonElement serialize(final Option src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isDefined()) {
				return context.serialize(src.get());
			}
			return null; //rien
		}
	}

	private static final class DefinitionReferenceJsonSerializer implements JsonSerializer<DefinitionReference> {
		@Override
		public JsonElement serialize(final DefinitionReference src, final Type typeOfSrc, final JsonSerializationContext context) {
			return context.serialize(src.get().getName());
		}
	}

	private static final class MapJsonSerializer implements JsonSerializer<Map> {
		@Override
		public JsonElement serialize(final Map src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class ListJsonSerializer implements JsonSerializer<List> {
		@Override
		public JsonElement serialize(final List src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class ComponentInfoJsonSerializer implements JsonSerializer<ComponentInfo> {
		@Override
		public JsonElement serialize(final ComponentInfo componentInfo, final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject jsonObject = new JsonObject();
			jsonObject.add(componentInfo.getTitle(), context.serialize(componentInfo.getValue()));
			return jsonObject;
		}
	}
}
