/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.vega;

import io.vertigo.app.config.Features;
import io.vertigo.app.config.PluginConfig;
import io.vertigo.app.config.PluginConfigBuilder;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;
import io.vertigo.vega.engines.webservice.json.GoogleJsonEngine;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.impl.token.TokenManagerImpl;
import io.vertigo.vega.impl.webservice.WebServiceManagerImpl;
import io.vertigo.vega.impl.webservice.catalog.CatalogWebServices;
import io.vertigo.vega.impl.webservice.catalog.SwaggerWebServices;
import io.vertigo.vega.plugins.webservice.handler.AccessTokenWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.AnalyticsWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.CorsAllowerWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ExceptionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.JsonConverterWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.PaginatorAndSortWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RateLimitingWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RestfulServiceWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SecurityWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ServerSideStateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionInvalidateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ValidatorWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.scanner.annotations.AnnotationsWebServiceScannerPlugin;
import io.vertigo.vega.plugins.webservice.webserver.sparkjava.SparkJavaEmbeddedWebServerPlugin;
import io.vertigo.vega.token.TokenManager;
import io.vertigo.vega.webservice.WebServiceManager;

/**
 * Defines module Vega.
 * @author pchretien
 */
public final class VegaFeatures extends Features {

	private boolean tokensEnabled;
	private String myTokens;
	private String mySearchApiVersion;
	private boolean miscEnabled;
	private boolean securityEnabled;
	private String myApiPrefix;
	private Integer myPort;
	private String myOriginCORSFilter;

	public VegaFeatures() {
		super("vega");
	}

	public VegaFeatures withTokens(final String tokens) {
		Assertion.checkArgNotEmpty(tokens);
		//-----
		tokensEnabled = true;
		myTokens = tokens;
		return this;
	}

	public VegaFeatures withMisc() {
		miscEnabled = true;
		return this;
	}

	public VegaFeatures withSecurity() {
		securityEnabled = true;
		return this;
	}

	public VegaFeatures withApiPrefix(final String apiPrefix) {
		myApiPrefix = apiPrefix;
		return this;
	}

	public VegaFeatures withSearchApiVersion(final String searchApiVersion) {
		mySearchApiVersion = searchApiVersion;
		return this;
	}

	public VegaFeatures withEmbeddedServer(final int port) {
		myPort = port;
		return this;
	}

	public VegaFeatures withOriginCORSFilter(final String originCORSFilter) {
		myOriginCORSFilter = originCORSFilter;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {

		final PluginConfigBuilder corsAllowerPluginConfigBuilder = new PluginConfigBuilder(CorsAllowerWebServiceHandlerPlugin.class);
		if (myOriginCORSFilter != null) {
			corsAllowerPluginConfigBuilder.addParam(Param.of("originCORSFilter", myOriginCORSFilter));
		}

		getModuleConfigBuilder()
				.withNoAPI()
				.addComponent(WebServiceManager.class, WebServiceManagerImpl.class)
				.addPlugin(AnnotationsWebServiceScannerPlugin.class)
				.addComponent(SwaggerWebServices.class)
				.addComponent(CatalogWebServices.class)

				//-- Handlers plugins
				.addPlugin(ExceptionWebServiceHandlerPlugin.class)
				.addPlugin(corsAllowerPluginConfigBuilder.build())
				.addPlugin(AnalyticsWebServiceHandlerPlugin.class)
				.addPlugin(JsonConverterWebServiceHandlerPlugin.class);
		if (mySearchApiVersion != null) {
			getModuleConfigBuilder()
					.addComponent(JsonEngine.class, GoogleJsonEngine.class,
							Param.of("searchApiVersion", mySearchApiVersion));
		} else {
			getModuleConfigBuilder()
					.addComponent(JsonEngine.class, GoogleJsonEngine.class);
		}

		if (securityEnabled) {
			getModuleConfigBuilder()
					.addPlugin(SessionInvalidateWebServiceHandlerPlugin.class)
					.addPlugin(SessionWebServiceHandlerPlugin.class)
					.addPlugin(SecurityWebServiceHandlerPlugin.class);
		}
		if (tokensEnabled) {
			getModuleConfigBuilder()
					.addPlugin(ServerSideStateWebServiceHandlerPlugin.class)
					.addPlugin(AccessTokenWebServiceHandlerPlugin.class)
					.addPlugin(PaginatorAndSortWebServiceHandlerPlugin.class)
					.addComponent(TokenManager.class, TokenManagerImpl.class,
							Param.of("collection", myTokens));
		}
		if (miscEnabled) {
			getModuleConfigBuilder()
					.addPlugin(RateLimitingWebServiceHandlerPlugin.class);
		}
		if (myPort != null) {
			final ListBuilder<Param> params = new ListBuilder()
					.add(Param.of("port", Integer.toString(myPort)));
			if (myApiPrefix != null) {
				params.add(Param.of("apiPrefix", myApiPrefix));
			}
			getModuleConfigBuilder().addPlugin(new PluginConfig(SparkJavaEmbeddedWebServerPlugin.class, params.build()));

		}

		getModuleConfigBuilder().addPlugin(ValidatorWebServiceHandlerPlugin.class)
				.addPlugin(RestfulServiceWebServiceHandlerPlugin.class);
	}
}
