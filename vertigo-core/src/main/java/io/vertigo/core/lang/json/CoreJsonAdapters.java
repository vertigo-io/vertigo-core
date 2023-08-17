/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.vertigo.core.node.definition.DefinitionId;

/**
 * Provides the Vertigo json serialization strategy for basic java types and types of vertigo-core
 * @author mlaroche
 *
 */
public class CoreJsonAdapters {

	public static final Gson V_CORE_GSON = addCoreGsonConfig(new GsonBuilder(), false).create();

	public static GsonBuilder addCoreGsonConfig(final GsonBuilder gsonBuilder, final boolean serializeNulls) {

		gsonBuilder
				.setPrettyPrinting()
				//.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.registerTypeAdapter(Date.class, new UTCDateAdapter())
				.registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
				.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
				.registerTypeAdapter(Instant.class, new InstantAdapter())
				.registerTypeAdapter(String.class, new EmptyStringAsNull())// add "" <=> null
				.registerTypeAdapter(TypeToken.get(Collections.EMPTY_MAP.getClass()).getType(), new EmptyMapAdapter())
				.registerTypeAdapter(TypeToken.get(Collections.EMPTY_LIST.getClass()).getType(), new EmptyListAdapter())
				.registerTypeAdapter(TypeToken.get(Collections.EMPTY_SET.getClass()).getType(), new EmptySetAdapter());

		if (!serializeNulls) {
			gsonBuilder
					.registerTypeAdapter(List.class, new ListJsonSerializer())
					.registerTypeAdapter(Map.class, new MapJsonSerializer());
		}

		gsonBuilder
				.registerTypeAdapter(DefinitionId.class, new DefinitionIdJsonSerializer())
				.registerTypeAdapter(Optional.class, new OptionJsonSerializer())
				.registerTypeAdapter(Class.class, new ClassJsonSerializer())
				.addSerializationExclusionStrategy(new JsonExclusionStrategy());
		return gsonBuilder;

	}

	private static class UTCDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Date date, final Type type, final JsonSerializationContext jsonSerializationContext) {
			//Use INPUT_DATE_FORMATS[0] => ISO8601 format
			return new JsonPrimitive(UTCDateUtil.format(date));
		}

		/** {@inheritDoc} */
		@Override
		public Date deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return UTCDateUtil.parse(jsonElement.getAsString());
		}
	}

	private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final LocalDate date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)); // "yyyy-mm-dd"
		}

		/** {@inheritDoc} */
		@Override
		public LocalDate deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return LocalDate.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
		}
	}

	private static class ZonedDateTimeAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final ZonedDateTime date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(date.format(DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")))); // "yyyy-mm-ddTHH:MI:SSZ"
		}

		/** {@inheritDoc} */
		@Override
		public ZonedDateTime deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return ZonedDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")));
		}
	}

	private static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Instant date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(UTCDateUtil.formatInstant(date)); // "yyyy-mm-ddTHH:MI:SSZ"
		}

		/** {@inheritDoc} */
		@Override
		public Instant deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return UTCDateUtil.parseInstant(jsonElement.getAsString());
		}
	}

	private static class EmptyStringAsNull implements JsonDeserializer<String> {

		/** {@inheritDoc} */
		@Override
		public String deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			final String value = jsonElement.getAsString();
			if (value != null && value.isEmpty()) {
				return null;
			}
			return value;
		}
	}

	private static class EmptyMapAdapter extends TypeAdapter<Map> {
		@Override
		public void write(final JsonWriter out, final Map value) throws IOException {
			out.beginObject().endObject();
		}

		@Override
		public Map read(final JsonReader in) throws IOException {
			in.beginObject();
			in.endObject();
			return Collections.emptyMap();
		}
	}

	private static class EmptySetAdapter extends TypeAdapter<Set> {
		@Override
		public void write(final JsonWriter out, final Set value) throws IOException {
			out.beginArray().endArray();
		}

		@Override
		public Set read(final JsonReader in) throws IOException {
			in.beginArray();
			in.endArray();
			return Collections.emptySet();
		}
	}

	private static class EmptyListAdapter extends TypeAdapter<List> {
		@Override
		public void write(final JsonWriter out, final List value) throws IOException {
			out.beginArray().endArray();
		}

		@Override
		public List read(final JsonReader in) throws IOException {
			in.beginArray();
			in.endArray();
			return Collections.emptyList();
		}
	}

	private static final class DefinitionIdJsonSerializer implements JsonSerializer<DefinitionId> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final DefinitionId src, final Type typeOfSrc, final JsonSerializationContext context) {
			return context.serialize(src.get().getName());
		}
	}

	private static final class MapJsonSerializer implements JsonSerializer<Map> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Map src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class ListJsonSerializer implements JsonSerializer<List> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final List src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class JsonExclusionStrategy implements ExclusionStrategy {
		/** {@inheritDoc} */
		@Override
		public boolean shouldSkipField(final FieldAttributes arg0) {
			return arg0.getAnnotation(JsonExclude.class) != null;
		}

		@Override
		public boolean shouldSkipClass(final Class<?> arg0) {
			return false;
		}
	}

	private static final class ClassJsonSerializer implements JsonSerializer<Class> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Class src, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(src.getName());
		}
	}

	private static final class OptionJsonSerializer implements JsonSerializer<Optional> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Optional src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isPresent()) {
				return context.serialize(src.get());
			}
			return null; //rien
		}
	}

}
