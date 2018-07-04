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
	
	exports io.vertigo.commons.impl.transaction.listener to io.vertigo.core;
	exports io.vertigo.commons.impl.analytics to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.hash to io.vertigo.core;
	exports io.vertigo.commons.impl.transaction to io.vertigo.core;
	exports io.vertigo.commons.impl.node to io.vertigo.core;
	exports io.vertigo.commons.impl.analytics.metric to io.vertigo.core;
	exports io.vertigo.commons.impl.eventbus to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.compression to io.vertigo.core;
	exports io.vertigo.commons.impl.daemon to io.vertigo.core;
	exports io.vertigo.commons.impl.analytics.process to io.vertigo.core;
	exports io.vertigo.commons.impl.script to io.vertigo.core;
	exports io.vertigo.commons.impl to io.vertigo.core;
	exports io.vertigo.commons.impl.cache to io.vertigo.core;
	exports io.vertigo.commons.plugins.node.registry.redis to io.vertigo.core;
	exports io.vertigo.commons.plugins.script.janino to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.crypto to io.vertigo.core;
	exports io.vertigo.commons.impl.connectors.redis to io.vertigo.core;
	exports io.vertigo.commons.plugins.cache.ehcache to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.csv to io.vertigo.core;
	exports io.vertigo.commons.plugins.node.registry.db to io.vertigo.core;
	exports io.vertigo.commons.impl.codec to io.vertigo.core;
	exports io.vertigo.commons.plugins.cache.memory to io.vertigo.core;
	exports io.vertigo.commons.plugins.node.infos.http to io.vertigo.core;
	exports io.vertigo.commons.plugins.cache.redis to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.compressedserialization to io.vertigo.core;
	exports io.vertigo.commons.plugins.node.registry.single to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.html to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.hex to io.vertigo.core;
	exports io.vertigo.commons.impl.analytics.health to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.base64 to io.vertigo.core;
	exports io.vertigo.commons.impl.codec.serialization to io.vertigo.core;
	exports io.vertigo.commons.plugins.analytics.log to io.vertigo.core;
	
	
	opens io.vertigo.commons to io.vertigo.core;
	opens io.vertigo.commons.script to io.vertigo.core;
	opens io.vertigo.commons.analytics to io.vertigo.core;
	opens io.vertigo.commons.analytics.health to io.vertigo.core, cglib.nodep;
	opens io.vertigo.commons.analytics.metric to io.vertigo.core;
	opens io.vertigo.commons.analytics.process to io.vertigo.core, cglib.nodep;
	opens io.vertigo.commons.daemon to io.vertigo.core, cglib.nodep;
	opens io.vertigo.commons.codec to io.vertigo.core;
	opens io.vertigo.commons.node to io.vertigo.core, gson;
	opens io.vertigo.commons.cache to io.vertigo.core;
	opens io.vertigo.commons.transaction to io.vertigo.core, cglib.nodep;
	opens io.vertigo.commons.eventbus to io.vertigo.core, cglib.nodep;
	opens io.vertigo.commons.peg to io.vertigo.core;
	opens io.vertigo.commons.script.parser to io.vertigo.core;
	
	
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