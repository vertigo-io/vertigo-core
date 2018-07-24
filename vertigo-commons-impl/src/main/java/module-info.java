/**
 *
 */
/**
 * @author mlaroche
 *
 */
module io.vertigo.commons {
	exports io.vertigo.commons;
	exports io.vertigo.commons.script;
	exports io.vertigo.commons.analytics;
	exports io.vertigo.commons.analytics.health;
	exports io.vertigo.commons.analytics.metric;
	exports io.vertigo.commons.analytics.process;
	exports io.vertigo.commons.daemon;
	exports io.vertigo.commons.codec;
	exports io.vertigo.commons.node;
	exports io.vertigo.commons.cache;
	exports io.vertigo.commons.transaction;
	exports io.vertigo.commons.eventbus;
	exports io.vertigo.commons.peg;
	exports io.vertigo.commons.script.parser;

	opens io.vertigo.commons.node to gson;
	opens io.vertigo.commons.impl.analytics.process to gson;

	requires c3p0;
	requires commons.compiler;
	requires commons.pool2;
	requires ehcache;
	requires gson;
	requires io.vertigo.core;
	requires janino;
	requires java.desktop;
	requires java.management;
	requires java.naming;
	requires java.sql;
	requires javax.inject;
	requires jedis;
	requires log4j.api;
	requires log4j.core;
}
