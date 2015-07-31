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

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.vega.impl.rest.RestHandlerPlugin;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.token.TokenManager;

import java.io.Serializable;

import javax.inject.Inject;

import spark.Request;
import spark.Response;

/**
 * Params handler. Extract and Json convert.
 * @author npiedeloup
 */
public final class AccessTokenRestHandlerPlugin implements RestHandlerPlugin {
	private static final Serializable TOKEN_DATA = new UniqueToken();
	/** Access Token header name. */
	private static final String HEADER_ACCESS_TOKEN = "x-access-token";
	private static final String INVALID_ACCESS_TOKEN_MSG = "Invalid access token"; //Todo make a resource.properties
	private final TokenManager tokenManager;

	/**
	 * Constructor.
	 * @param tokenManager TokenManager
	 */
	@Inject
	public AccessTokenRestHandlerPlugin(final TokenManager tokenManager) {
		Assertion.checkNotNull(tokenManager);
		//-----
		this.tokenManager = tokenManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final EndPointDefinition endPointDefinition) {
		return endPointDefinition.isAccessTokenMandatory() || endPointDefinition.isAccessTokenConsume() || endPointDefinition.isAccessTokenPublish();
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		final String accessTokenKey;
		if (routeContext.getEndPointDefinition().isAccessTokenMandatory()) {
			accessTokenKey = request.headers(HEADER_ACCESS_TOKEN);
			if (accessTokenKey == null) {
				throw new VSecurityException(INVALID_ACCESS_TOKEN_MSG); //same message for no AccessToken or bad AccessToken
			}
			final Option<Serializable> tokenData = tokenManager.get(accessTokenKey);
			if (tokenData.isEmpty()) {
				throw new VSecurityException(INVALID_ACCESS_TOKEN_MSG); //same message for no AccessToken or bad AccessToken
			}
		} else {
			accessTokenKey = null;
		}
		final Object result = chain.handle(request, response, routeContext);
		if (accessTokenKey != null && routeContext.getEndPointDefinition().isAccessTokenConsume()) {
			tokenManager.getAndRemove(accessTokenKey);
		}
		if (routeContext.getEndPointDefinition().isAccessTokenPublish()) {
			final String newAccessTokenKey = tokenManager.put(TOKEN_DATA);
			response.header(HEADER_ACCESS_TOKEN, newAccessTokenKey);
		}
		return result;
	}

	private static class UniqueToken implements Serializable {
		private static final long serialVersionUID = 1L;

		public UniqueToken() {
			//empty
		}
	}
}
