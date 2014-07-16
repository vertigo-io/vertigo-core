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

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;

import java.util.ArrayList;
import java.util.List;

import spark.Request;
import spark.Response;

/**
 * Chain of handlers to handle a Request.
 * @author npiedeloup
 */
final class HandlerChain {
	private final List<RouteHandler> handlerList;
	private final int offset;
	private boolean isLock;

	/**
	 * Constructor.
	 */
	public HandlerChain() {
		handlerList = new ArrayList<>();
		offset = 0;
	}

	/**
	 * private constructor for go forward in chain
	 * @param previous chain
	 */
	private HandlerChain(final HandlerChain previous) {
		Assertion.checkState(previous.offset < 50, "HandlerChain go through 50 handlers. Force halt : infinit loop suspected.");
		//---------------------------------------------------------------------
		handlerList = previous.handlerList;
		offset = previous.offset + 1; //on avance
		isLock = true;
	}

	/**
	 * Do handle of this route.
	 * 
	 * @param request spark.Request
	 * @param response spark.Response
	 */
	Object handle(final Request request, final Response response) throws VSecurityException, SessionException {
		isLock = true;
		if (offset < handlerList.size()) {
			final RouteHandler nextHandler = handlerList.get(offset);
			//System.out.println(">>> before doFilter " + nextHandler);
			return nextHandler.handle(request, response, new HandlerChain(this));
			//System.out.println("<<< after doFilter " + nextHandler);
		}
		throw new RuntimeException("Last routeHandler haven't send response body");
	}

	/**
	 * Add an handler to this chain (only during init phase).
	 * @param newHandler Handler to add
	 */
	public void addHandler(final RouteHandler newHandler) {
		Assertion.checkNotNull(newHandler);
		Assertion.checkState(!isLock, "Can't add handler to a already used chain");
		//---------------------------------------------------------------------	
		//System.out.println("+++ addHandler " + newHandler);
		handlerList.add(newHandler);
	}

}
