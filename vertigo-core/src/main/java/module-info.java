/**
 * @author mlaroche
 *
 */
module io.vertigo.core {
	exports io.vertigo.core.plugins.param.properties;
	exports io.vertigo.core.plugins.param.env;
	exports io.vertigo.core.component.di.injector;
	exports io.vertigo.core.definition.loader;
	exports io.vertigo.core.component.loader;
	exports io.vertigo.core.param;
	exports io.vertigo.core.locale;
	exports io.vertigo.core.component;
	exports io.vertigo.lang;
	exports io.vertigo.core.resource;
	exports io.vertigo.core.plugins.resource.local;
	exports io.vertigo.core.plugins.resource.url;
	exports io.vertigo.util;
	exports io.vertigo.app;
	exports io.vertigo.core.component.aop;
	exports io.vertigo.core.plugins.resource.classpath;
	exports io.vertigo.app.config.xml;
	exports io.vertigo.core.plugins.component.aop.cglib;
	exports io.vertigo.core.component.di.reactor;
	exports io.vertigo.core.plugins.param.xml;
	exports io.vertigo.core.component.di;
	exports io.vertigo.app.config.discovery;
	exports io.vertigo.core.definition;
	exports io.vertigo.app.config;
	exports io.vertigo.core.component.proxy;
	
	opens io.vertigo.core.component.aop to cglib.nodep;
	
	requires cglib.nodep;
	requires guava;
	requires java.desktop;
	requires java.xml;
	requires javax.inject;
	requires log4j.api;
	requires log4j.core;
	requires reflections;

	
		
}