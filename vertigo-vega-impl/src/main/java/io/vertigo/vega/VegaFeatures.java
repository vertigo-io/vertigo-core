package io.vertigo.vega;

import io.vertigo.core.config.Features;
import io.vertigo.vega.impl.rest.WebServiceManagerImpl;
import io.vertigo.vega.impl.rest.catalog.CatalogWebServices;
import io.vertigo.vega.impl.rest.catalog.SwaggerWebServices;
import io.vertigo.vega.impl.token.TokenManagerImpl;
import io.vertigo.vega.plugins.rest.handler.AccessTokenWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.CorsAllowerWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ExceptionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.JsonConverterWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.PaginatorAndSortWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.RateLimitingWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.RestfulServiceWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SecurityWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ServerSideStateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionInvalidateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ValidatorWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.instrospector.annotations.AnnotationsWebServiceIntrospectorPlugin;
import io.vertigo.vega.plugins.rest.webserver.sparkjava.SparkJavaEmbeddedWebServerPlugin;
import io.vertigo.vega.rest.WebServiceManager;
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
				.addComponent(WebServiceManager.class, WebServiceManagerImpl.class)
				.addPlugin(AnnotationsWebServiceIntrospectorPlugin.class)
				.addComponent(SwaggerWebServices.class)
				.addComponent(CatalogWebServices.class)

				//-- Handlers plugins
				.addPlugin(ExceptionWebServiceHandlerPlugin.class)
				.addPlugin(CorsAllowerWebServiceHandlerPlugin.class)
				.addPlugin(SessionInvalidateWebServiceHandlerPlugin.class)
				.addPlugin(SessionWebServiceHandlerPlugin.class)
				.addPlugin(SecurityWebServiceHandlerPlugin.class)
				//.beginPlugin(OldJsonConverterWebServiceHandlerPlugin.class).endPlugin()
				.addPlugin(JsonConverterWebServiceHandlerPlugin.class)
				.addPlugin(ServerSideStateWebServiceHandlerPlugin.class)
				.addPlugin(ValidatorWebServiceHandlerPlugin.class)
				.addPlugin(RestfulServiceWebServiceHandlerPlugin.class);
	}

	public VegaFeatures withTokens() {
		getModuleConfigBuilder()
				.addPlugin(AccessTokenWebServiceHandlerPlugin.class)
				.beginComponent(TokenManager.class, TokenManagerImpl.class)
				.addParam("dataStoreName", "UiSecurityStore")
				.endComponent();
		return this;
	}

	public VegaFeatures withMisc() {
		getModuleConfigBuilder()
				.addPlugin(PaginatorAndSortWebServiceHandlerPlugin.class)
				.addPlugin(RateLimitingWebServiceHandlerPlugin.class);
		return this;
	}

	public VegaFeatures withEmbeddedServer(final int port) {
		getModuleConfigBuilder().beginPlugin(SparkJavaEmbeddedWebServerPlugin.class)
				.addParam("port", Integer.toString(port))
				.endPlugin();
		return this;
	}
}
