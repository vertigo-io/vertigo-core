/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import javax.inject.Named;

import io.vertigo.app.config.Features;
import io.vertigo.app.config.PluginConfig;
import io.vertigo.app.config.PluginConfigBuilder;
import io.vertigo.app.config.json.Feature;
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
import io.vertigo.vega.plugins.webservice.webserver.sparkjava.SparkJavaServletFilterWebServerPlugin;
import io.vertigo.vega.token.TokenManager;
import io.vertigo.vega.webservice.WebServiceManager;

/**
 * Defines module Vega.
 * @author pchretien
 */
public final class VegaFeatures extends Features<VegaFeatures> {

	private boolean tokensEnabled;
	private String myTokens;
	private String mySearchApiVersion;
	private boolean rateLimitingEnabled;
	private boolean securityEnabled;
	private String myApiPrefix;
	private String myPort;
	private String myOriginCORSFilter;

	public VegaFeatures() {
		super("vega");
	}

	@Feature("token")
	public VegaFeatures withTokens(final @Named("tokens") String tokens) {
		Assertion.checkArgNotEmpty(tokens);
		//-----
		tokensEnabled = true;
		myTokens = tokens;
		return this;
	}

	@Feature("rateLimiting")
	public VegaFeatures withRateLimiting() {
		rateLimitingEnabled = true;
		return this;
	}

	@Feature("security")
	public VegaFeatures withSecurity() {
		securityEnabled = true;
		return this;
	}

	@Feature("apiPrefix")
	public VegaFeatures withApiPrefix(final @Named("apiPrefix") String apiPrefix) {
		myApiPrefix = apiPrefix;
		return this;
	}

	@Feature("searchApiVersion")
	public VegaFeatures withSearchApiVersion(final @Named("searchApiVersion") String searchApiVersion) {
		mySearchApiVersion = searchApiVersion;
		return this;
	}

	@Feature("embeddedServer")
	public VegaFeatures withEmbeddedServer(final @Named("port") String port) {
		myPort = port;
		return this;
	}

	@Feature("cors")
	public VegaFeatures withOriginCORSFilter(final @Named("originCORSFilter") String originCORSFilter) {
		myOriginCORSFilter = originCORSFilter;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {

		final PluginConfigBuilder corsAllowerPluginConfigBuilder = PluginConfig.builder(CorsAllowerWebServiceHandlerPlugin.class);
		if (myOriginCORSFilter != null) {
			corsAllowerPluginConfigBuilder.addParam(Param.of("originCORSFilter", myOriginCORSFilter));
		}

		getModuleConfigBuilder()
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
		if (rateLimitingEnabled) {
			getModuleConfigBuilder()
					.addPlugin(RateLimitingWebServiceHandlerPlugin.class);
		}
		if (myPort != null) {
			final ListBuilder<Param> params = new ListBuilder()
					.add(Param.of("port", myPort));
			if (myApiPrefix != null) {
				params.add(Param.of("apiPrefix", myApiPrefix));
			}
			getModuleConfigBuilder().addPlugin(new PluginConfig(SparkJavaEmbeddedWebServerPlugin.class, params.build()));
		} else {
			final ListBuilder<Param> params = new ListBuilder<>();
			if (myApiPrefix != null) {
				params.add(Param.of("apiPrefix", myApiPrefix));
			}
			getModuleConfigBuilder().addPlugin(new PluginConfig(SparkJavaServletFilterWebServerPlugin.class, params.build()));
		}

		getModuleConfigBuilder().addPlugin(ValidatorWebServiceHandlerPlugin.class)
				.addPlugin(RestfulServiceWebServiceHandlerPlugin.class);
	}
}
