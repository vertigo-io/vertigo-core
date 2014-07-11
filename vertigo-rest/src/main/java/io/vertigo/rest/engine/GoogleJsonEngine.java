package io.vertigo.rest.engine;

import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.engines.JsonEngine;
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

/**
 * @author pchretien
 */
public final class GoogleJsonEngine implements JsonEngine {
	private final Gson gson = createGson();

	@Override
	public String toJson(Object data) {
		return gson.toJson(data);
	}

	private static Gson createGson() {
		return new GsonBuilder()//
				.setPrettyPrinting()//
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
