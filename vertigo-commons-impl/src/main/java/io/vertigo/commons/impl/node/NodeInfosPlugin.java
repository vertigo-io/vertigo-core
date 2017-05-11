package io.vertigo.commons.impl.node;

import java.util.Map;

import io.vertigo.commons.node.Node;
import io.vertigo.lang.Plugin;

/**
 * Plugin for retrieving infos about a node.
 * @author mlaroche
 *
 */
public interface NodeInfosPlugin extends Plugin {

	// TODO : à terme AppConfig
	String getConfig(Node node);

	String getStatus(Node node);

	Map<String, Object> getStats(Node node);

	String getProtocol();

}
