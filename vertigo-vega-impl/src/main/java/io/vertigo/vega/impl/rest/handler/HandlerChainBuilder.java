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
