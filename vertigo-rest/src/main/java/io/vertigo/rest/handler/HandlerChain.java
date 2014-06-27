package io.vertigo.rest.handler;

import io.vertigo.kernel.exception.VRuntimeException;
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
		throw new VRuntimeException("Last routeHandler haven't send response body");
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
