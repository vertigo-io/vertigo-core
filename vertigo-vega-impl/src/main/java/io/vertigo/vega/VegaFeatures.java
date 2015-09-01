package io.vertigo.vega;

import io.vertigo.core.config.Features;
import io.vertigo.vega.impl.rest.RestManagerImpl;
import io.vertigo.vega.impl.rest.catalog.CatalogRestServices;
import io.vertigo.vega.impl.rest.catalog.SwaggerWebServices;
import io.vertigo.vega.impl.token.TokenManagerImpl;
import io.vertigo.vega.plugins.rest.handler.AccessTokenRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.CorsAllowerRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ExceptionRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.JsonConverterRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.PaginatorAndSortRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.RateLimitingRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.RestfulServiceRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SecurityRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ServerSideStateRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionInvalidateRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ValidatorRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.instrospector.annotations.AnnotationsEndPointIntrospectorPlugin;
import io.vertigo.vega.plugins.rest.webserver.sparkjava.SparkJavaEmbeddedWebServerPlugin;
import io.vertigo.vega.rest.RestManager;
import io.vertigo.vega.rest.engine.GoogleJsonEngine;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.token.TokenManager;

/**
 * Defines module Vega.
 * @author pchretien
 */
public final class VegaFeatures extends Features {

	public VegaFeatures() {
		super("vega");
	}

	@Override
	protected void setUp() {
		getModuleConfigBuilder()
				.withNoAPI().withInheritance(Object.class)
				.addComponent(JsonEngine.class, GoogleJsonEngine.class)
				.addComponent(RestManager.class, RestManagerImpl.class)
				.addPlugin(AnnotationsEndPointIntrospectorPlugin.class)
				.addComponent(SwaggerWebServices.class)
				.addComponent(CatalogRestServices.class)

				//-- Handlers plugins
				.addPlugin(ExceptionRestHandlerPlugin.class)
				.addPlugin(CorsAllowerRestHandlerPlugin.class)
				.addPlugin(SessionInvalidateRestHandlerPlugin.class)
				.addPlugin(SessionRestHandlerPlugin.class)
				.addPlugin(SecurityRestHandlerPlugin.class)
				//.beginPlugin(OldJsonConverterRestHandlerPlugin.class).endPlugin()
				.addPlugin(JsonConverterRestHandlerPlugin.class)
				.addPlugin(ValidatorRestHandlerPlugin.class)
				.addPlugin(RestfulServiceRestHandlerPlugin.class);
	}

	public VegaFeatures withTokens() {
		getModuleConfigBuilder()
				.addPlugin(AccessTokenRestHandlerPlugin.class)
				.beginComponent(TokenManager.class, TokenManagerImpl.class)
				.addParam("dataStoreName", "UiSecurityStore")
				.endComponent();
		return this;
	}

	public VegaFeatures withMisc() {
		getModuleConfigBuilder()
				.addPlugin(ServerSideStateRestHandlerPlugin.class)
				.addPlugin(PaginatorAndSortRestHandlerPlugin.class)
				.addPlugin(RateLimitingRestHandlerPlugin.class);
		return this;
	}

	public VegaFeatures withEmbeddedServer(final int port) {
		getModuleConfigBuilder().beginPlugin(SparkJavaEmbeddedWebServerPlugin.class)
				.addParam("port", Integer.toString(port))
				.endPlugin();
		return this;
	}
}
