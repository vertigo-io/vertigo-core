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
package io.vertigo.vega.plugins.rest.handler;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.vega.impl.rest.RestHandlerPlugin;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.TooManyRequestException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Named;

import spark.Request;
import spark.Response;

/**
 * Rate limit handler.
 * @author npiedeloup
 */
public final class RateLimitingRestHandlerPlugin implements RestHandlerPlugin {
	private static final long DEFAULT_LIMIT_VALUE = 150; //the rate limit ceiling value
	private static final int DEFAULT_WINDOW_SECONDS = 5 * 60; //the time windows use to limit calls rate
	private static final String RATE_LIMIT_LIMIT = "X-Rate-Limit-Limit"; //the rate limit ceiling for that given request
	private static final String RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining"; //the number of requests left for the M minute window
	private static final String RATE_LIMIT_RESET = "X-Rate-Limit-Reset"; //the remaining seconds before the rate limit resets

	private final VSecurityManager securityManager;
	private final int windowSeconds;
	private final long limitValue;

	/**
	 * Hit counter by userKey.
	 */
	final ConcurrentMap<String, AtomicLong> hitsCounter = new ConcurrentHashMap<>();
	/**
	 * Last window start time.
	 */
	long lastRateLimitResetTime = System.currentTimeMillis();

	/**
	 * Constructor.
	 * @param windowSeconds the time windows use to limit calls rate
	 * @param limitValue the rate limit ceiling value
	 * @param securityManager Security Manager
	 * @param daemonManager Manager des daemons
	 */
	@Inject
	public RateLimitingRestHandlerPlugin(final VSecurityManager securityManager, final DaemonManager daemonManager, @Named("windowSeconds") final Option<Integer> windowSeconds, @Named("limitValue") final Option<Long> limitValue) {
		Assertion.checkNotNull(securityManager);
		Assertion.checkNotNull(limitValue);
		Assertion.checkNotNull(windowSeconds);
		//-----
		this.securityManager = securityManager;
		this.limitValue = limitValue.getOrElse(DEFAULT_LIMIT_VALUE);
		this.windowSeconds = windowSeconds.getOrElse(DEFAULT_WINDOW_SECONDS);
		//RateLimitingRestHandlerPlugin::resetRateLimitWindow
		daemonManager.registerDaemon("rateLimitWindowReset", RateLimitWindowResetDaemon.class, this.windowSeconds, this);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final EndPointDefinition endPointDefinition) {
		return true;
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		Assertion.checkNotNull(request);
		Assertion.checkNotNull(response);
		Assertion.checkNotNull(routeContext);
		Assertion.checkNotNull(chain);
		//-----
		final String userKey = obtainUserKey(request, securityManager.getCurrentUserSession());
		response.header(RATE_LIMIT_LIMIT, String.valueOf(limitValue));
		response.header(RATE_LIMIT_RESET, String.valueOf(windowSeconds - (System.currentTimeMillis() - lastRateLimitResetTime) / 1000));

		final long hits = touch(userKey);
		if (hits > limitValue) {
			throw new TooManyRequestException("Rate limit exceeded");
		}
		response.header(RATE_LIMIT_REMAINING, String.valueOf(limitValue - hits));
		return chain.handle(request, response, routeContext);
	}

	private static String obtainUserKey(final Request request, final Option<UserSession> userSession) {
		if (userSession.isDefined()) {
			return userSession.get().getSessionUUID().toString();
		}
		return request.ip() + ":" + request.headers("user-agent");
	}

	private long touch(final String userKey) {
		final AtomicLong value = new AtomicLong(0);
		final AtomicLong oldValue = hitsCounter.putIfAbsent(userKey, value);
		return (oldValue != null ? oldValue : value).incrementAndGet();
	}

	/**
	 * Reset current limitWindow.
	 */
	void resetRateLimitWindow() {
		hitsCounter.clear();
		lastRateLimitResetTime = System.currentTimeMillis();
	}

	/**
	 * @author npiedeloup
	 */
	public static final class RateLimitWindowResetDaemon implements Daemon {
		private final RateLimitingRestHandlerPlugin rateLimitingRestHandlerPlugin;

		/**
		 * @param rateLimitingRestHandlerPlugin This plugin
		 */
		public RateLimitWindowResetDaemon(final RateLimitingRestHandlerPlugin rateLimitingRestHandlerPlugin) {
			Assertion.checkNotNull(rateLimitingRestHandlerPlugin);
			//------
			this.rateLimitingRestHandlerPlugin = rateLimitingRestHandlerPlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			rateLimitingRestHandlerPlugin.resetRateLimitWindow();
		}
	}

}
