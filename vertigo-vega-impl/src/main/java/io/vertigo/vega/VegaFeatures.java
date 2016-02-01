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
package io.vertigo.vega;

import io.vertigo.app.config.Features;
import io.vertigo.lang.Assertion;
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

	private boolean withTokens = false;
	private String tokenCollection;
	private boolean withMisc = false;
	private boolean securityEnabled = false;

	public VegaFeatures() {
		super("vega");
	}

	@Override
	protected void setUp() {
		//rien
	}

	public VegaFeatures withTokens(final String collection) {
		Assertion.checkArgNotEmpty(collection);
		//-----
		withTokens = true;
		tokenCollection = collection;
		return this;
	}

	public VegaFeatures withMisc() {
		withMisc = true;
		return this;
	}

	public VegaFeatures withSecurity() {
		securityEnabled = true;
		return this;
	}

	public VegaFeatures withEmbeddedServer(final int port) {
		getModuleConfigBuilder().beginPlugin(SparkJavaEmbeddedWebServerPlugin.class)
				.addParam("port", Integer.toString(port))
				.endPlugin();
		return this;
	}

	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.withNoAPI()
				.addComponent(JsonEngine.class, GoogleJsonEngine.class)
				.addComponent(WebServiceManager.class, WebServiceManagerImpl.class)
				.addPlugin(AnnotationsWebServiceScannerPlugin.class)
				.addComponent(SwaggerWebServices.class)
				.addComponent(CatalogWebServices.class)

				//-- Handlers plugins
				.addPlugin(ExceptionWebServiceHandlerPlugin.class)
				.addPlugin(CorsAllowerWebServiceHandlerPlugin.class)
				.addPlugin(AnalyticsWebServiceHandlerPlugin.class)
				.addPlugin(JsonConverterWebServiceHandlerPlugin.class);
		if (securityEnabled) {
			getModuleConfigBuilder()
					.addPlugin(SessionInvalidateWebServiceHandlerPlugin.class)
					.addPlugin(SessionWebServiceHandlerPlugin.class)
					.addPlugin(SecurityWebServiceHandlerPlugin.class);
		}
		if (withTokens) {
			getModuleConfigBuilder().addPlugin(ServerSideStateWebServiceHandlerPlugin.class)
					.addPlugin(AccessTokenWebServiceHandlerPlugin.class)
					.beginComponent(TokenManager.class, TokenManagerImpl.class)
					.addParam("collection", tokenCollection)
					.endComponent();
		}
		if (withMisc) {
			getModuleConfigBuilder()
					.addPlugin(PaginatorAndSortWebServiceHandlerPlugin.class)
					.addPlugin(RateLimitingWebServiceHandlerPlugin.class);
		}
		getModuleConfigBuilder().addPlugin(ValidatorWebServiceHandlerPlugin.class)
				.addPlugin(RestfulServiceWebServiceHandlerPlugin.class);
	}
}
