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
package io.vertigo.rest.impl.rest.handler;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.rest.rest.engine.GoogleJsonEngine;
import io.vertigo.rest.rest.engine.JsonEngine;
import io.vertigo.rest.rest.engine.UiContext;
import io.vertigo.rest.security.UiSecurityTokenManager;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

final class JsonConverterHelper {
	private final JsonEngine jsonReaderEngine;
	private final JsonEngine jsonWriterEngine;

	 public JsonConverterHelper(final JsonEngine jsonReaderEngine, final JsonEngine jsonWriterEngine) {
		Assertion.checkNotNull(jsonReaderEngine);
		Assertion.checkNotNull(jsonWriterEngine);
		//---------------------------------------------------------------------
		this.jsonReaderEngine = jsonReaderEngine;
		this.jsonWriterEngine = jsonWriterEngine;
	 }
 
 
  public String writeValue(final Object value, boolean serverSideSave, UiSecurityTokenManager uiSecurityTokenManager, Set<String> includedFields, Set<String> excludedFields) {
		Assertion.checkNotNull(value);
		//---------------------------------------------------------------------
		if (serverSideSave) {
			if (UiContext.class.isInstance(value)) {
				//TODO build json in jsonWriterEngine 
				final StringBuilder sb = new StringBuilder();
				sb.append("{");
				String sep = "";
				for (final Map.Entry<String, Serializable> entry : ((UiContext) value).entrySet()) {
					sb.append(sep);
					String encodedValue;
					if (entry.getValue() instanceof DtList || entry.getValue() instanceof DtObject) {
						encodedValue = writeValue(entry.getValue(), serverSideSave, uiSecurityTokenManager, includedFields, excludedFields);
					} else {
						encodedValue = jsonWriterEngine.toJson(entry.getValue());
					}
					sb.append("\"").append(entry.getKey()).append("\":").append(encodedValue).append("");
					sep = ", ";
				}
				sb.append("}");
				return sb.toString();
			} else if (DtList.class.isInstance(value)) {
				final String tokenId = uiSecurityTokenManager.put((DtList) value);
				return jsonWriterEngine.toJsonWithTokenId(value, tokenId, includedFields, excludedFields);
			} else if (DtObject.class.isInstance(value)) {
				final String tokenId = uiSecurityTokenManager.put((DtObject) value);
				return jsonWriterEngine.toJsonWithTokenId(value, tokenId, includedFields, excludedFields);
			} else {
				throw new RuntimeException("Return type can't be ServerSide :" + (value != null ? value.getClass().getSimpleName() : "null"));
			}
		}
		return jsonWriterEngine.toJson(value, includedFields, excludedFields);
	}
}
