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
package io.vertigo.rest.handler;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.rest.EndPointDefinition;
import io.vertigo.rest.EndPointParam;
import io.vertigo.rest.engine.UiObject;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;
import io.vertigo.rest.validation.DtObjectValidator;
import io.vertigo.rest.validation.UiMessageStack;
import io.vertigo.rest.validation.ValidationUserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.Response;

/**
 * Auto paginator and Sort handler.
 * @author npiedeloup
 */
final class PaginatorAndSortHandler implements RouteHandler {
	private final EndPointDefinition endPointDefinition;

	PaginatorAndSortHandler(final EndPointDefinition endPointDefinition) {
		Assertion.checkNotNull(endPointDefinition);
		//---------------------------------------------------------------------
		this.endPointDefinition = endPointDefinition;
	}

	/** {@inheritDoc}  */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		Object result = chain.handle(request, response, routeContext);
		if(result instanceof List) {
			
		}
		return result;
	}

	private class ListWithMeta {
		private final Map<String, Object> metas = new HashMap<>();
		private final String listValue;
		
		
		ListWithMeta(String listValue) {
			Assertion.checkArgNotEmpty(listValue);
			//-----------------------------------------------------------------
			this.listValue = listValue;
		}
		
		void addMeta(String key, Object value) {
			metas.put(key, value);
		}
		
	}
}
