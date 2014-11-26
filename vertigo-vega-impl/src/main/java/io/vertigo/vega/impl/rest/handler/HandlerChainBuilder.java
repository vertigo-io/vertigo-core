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
package io.vertigo.vega.impl.rest.handler;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.ArrayList;
import java.util.List;

public final class HandlerChainBuilder implements Builder<HandlerChain> {
	private final List<RouteHandler> myHandlers = new ArrayList<>();

	public HandlerChainBuilder withHandler(final boolean test, final RouteHandler routeHandler) {
		if (test) {
			return withHandler(routeHandler);
		}
		return this;
	}

	public HandlerChainBuilder withHandler(final RouteHandler routeHandler) {
		Assertion.checkNotNull(routeHandler);
		//---------------------------------------------------------------------
		myHandlers.add(routeHandler);
		return this;
	}

	@Override
	public HandlerChain build() {
		return new HandlerChain(myHandlers);
	}
}
