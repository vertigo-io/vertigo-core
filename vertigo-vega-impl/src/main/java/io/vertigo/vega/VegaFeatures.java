package io.vertigo.vega;

import io.vertigo.core.config.Features;
import io.vertigo.vega.engines.webservice.json.GoogleJsonEngine;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.impl.token.TokenManagerImpl;
import io.vertigo.vega.impl.webservice.WebServiceManagerImpl;
import io.vertigo.vega.impl.webservice.catalog.CatalogWebServices;
import io.vertigo.vega.impl.webservice.catalog.SwaggerWebServices;
import io.vertigo.vega.plugins.webservice.handler.AccessTokenWebServiceHandlerPlugin;
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
import io.vertigo.vega.plugins.webservice.instrospector.annotations.AnnotationsWebServiceIntrospectorPlugin;
import io.vertigo.vega.plugins.webservice.webserver.sparkjava.SparkJavaEmbeddedWebServerPlugin;
import io.vertigo.vega.token.TokenManager;
import io.vertigo.vega.webservice.WebServiceManager;

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
