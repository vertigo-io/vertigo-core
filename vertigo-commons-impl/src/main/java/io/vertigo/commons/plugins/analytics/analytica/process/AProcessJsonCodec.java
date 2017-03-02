/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.vertigo.commons.plugins.analytics.analytica.process;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/**
 * Codec used to convert KProcess to Json and Json to KProcess.
 * @author pchretien, npiedeloup
 */
public final class AProcessJsonCodec {

	private static final AProcessDeserializer PROCESS_DESERIALIZER = new AProcessDeserializer();

	/**
	 * Convert a KProcess list to Json String.
	 * @param processes Process list
	 * @return Json String
	 */
	public static String toJson(final List<AProcess> processes) {
		return new Gson().toJson(processes);
	}

	/**
	 * Convert a Json String to KProcess list.
	 * @param json Json string
	 * @return Process list
	 */
	public static List<AProcess> fromJson(final String json) {
		final Gson gson = new GsonBuilder().registerTypeAdapter(AProcess.class, PROCESS_DESERIALIZER).create();
		return gson.fromJson(json, AProcessDeserializer.LIST_PROCESS_TYPE);
	}

	/**
	 * Gson deserializer for KProcess Object.
	 * @author npiedeloup
	 */
	static final class AProcessDeserializer implements JsonDeserializer<AProcess> {

		/**
		 * Type List<KProcess>.
		 */
		public static final Type LIST_PROCESS_TYPE = new TypeToken<List<AProcess>>() { //empty
		}.getType();
		private static final Type MAP_STRING_DOUBLE_TYPE = new TypeToken<Map<String, Double>>() { //empty
		}.getType();
		private static final Type MAP_STRING_SET_STRING_TYPE = new TypeToken<Map<String, Set<String>>>() { //empty
		}.getType();
		private static final String[] EMPTY_STRING_ARRAY = new String[0];

		/** {@inheritDoc} */
		@Override
		public AProcess deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			//{"type":"COMMANDE","subTypes":["5 Commandes"],"startDate":"Mar 12, 2014 2:37:48 PM",
			// "measures":{"sub_duration":3.0,"HMDURATION":4.0},"metaDatas":{},"subProcesses":[]}
			final JsonObject jsonObject = json.getAsJsonObject();

			final JsonPrimitive jsonAppName = jsonObject.getAsJsonPrimitive("appName");
			//check nullity for compatibility
			final String appName = jsonAppName != null ? jsonAppName.getAsString() : "UnknownApp";

			final JsonPrimitive jsonType = jsonObject.getAsJsonPrimitive("type");
			final String type = jsonType.getAsString();

			final JsonArray jsonCategoryTerms = jsonObject.getAsJsonArray("categoryTerms");
			final String[] categoryTerms = deserialize(context, jsonCategoryTerms, String[].class, EMPTY_STRING_ARRAY);

			final JsonPrimitive jsonStartDate = jsonObject.getAsJsonPrimitive("startDate");
			final Date startDate = context.deserialize(jsonStartDate, Date.class);

			final JsonObject jsonMeasures = jsonObject.getAsJsonObject("measures");
			final Map<String, Double> measures = deserialize(context, jsonMeasures, MAP_STRING_DOUBLE_TYPE, Collections.<String, Double> emptyMap());

			final double durationMs = measures.get(AProcess.DURATION);

			final JsonObject jsonMetaDatas = jsonObject.getAsJsonObject("metaDatas");
			final Map<String, Set<String>> metaDatas = deserialize(context, jsonMetaDatas, MAP_STRING_SET_STRING_TYPE, Collections.<String, Set<String>> emptyMap());

			final JsonArray jsonSubProcesses = jsonObject.getAsJsonArray("subProcesses");
			final List<AProcess> processes = deserialize(context, jsonSubProcesses, LIST_PROCESS_TYPE, Collections.<AProcess> emptyList());

			final AProcessBuilder builder = new AProcessBuilder(appName, type, startDate, durationMs);

			for (final AProcess kProcess : processes) {
				builder.addSubProcess(kProcess);
			}

			for (final Map.Entry<String, Double> measure : measures.entrySet()) {
				builder.setMeasure(measure.getKey(), measure.getValue());
			}
			for (final Map.Entry<String, Set<String>> metaData : metaDatas.entrySet()) {
				builder.addMetaData(metaData.getKey(), metaData.getValue());
			}

			builder.withCategory(categoryTerms);
			return builder.build();
		}

		private static <O> O deserialize(final JsonDeserializationContext context, final JsonElement jsonElement, final Type typeOf, final O defaultValue) {
			if (jsonElement != null) {
				return context.<O> deserialize(jsonElement, typeOf);
			}
			return defaultValue;
		}
	}
}
