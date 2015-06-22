package io.vertigo.dynamo.plugins.search.elasticsearch.transport;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.plugins.search.elasticsearch.AbstractESSearchServicesPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import javax.inject.Inject;
import javax.inject.Named;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Gestion de la connexion au serveur elasticSearch en mode HTTP. Utilisation du client Transport simple, sans
 * intégration au cluster (permet de ne pas avoir de liaison bi-directionelle entre le tomcat et l'ES externe).
 *
 * @author skerdudou
 */
public final class ESTransportSearchServicesPlugin extends AbstractESSearchServicesPlugin {

	/** url du serveur elasticSearch. */
	private final String[] serversNames;
	/** cluster à rejoindre. */
	private final String clusterName;
	/** Nom du noeud. */
	private final String nodeName;
	/** le noeud interne. */
	private TransportClient client;

	/**
	 * Constructeur.
	 *
	 * @param serversNamesStr URL du serveur ElasticSearch avec le port de communication de cluster (9300 en général)
	 * @param cores Liste des indexes
	 * @param rowsPerQuery Liste des indexes
	 * @param codecManager Manager des codecs
	 * @param clusterName : nom du cluster à rejoindre
	 * @param nodeName : nom du node
	 * @param configFile fichier de configuration des index
	 * @param resourceManager Manager d'accès aux ressources
	 */
	@Inject
	public ESTransportSearchServicesPlugin(@Named("servers.names") final String serversNamesStr,
			@Named("cores") final String cores, @Named("rowsPerQuery") final int rowsPerQuery,
			@Named("cluster.name") final String clusterName, @Named("node.name") final Option<String> nodeName,
			@Named("config.file") final Option<String> configFile, final CodecManager codecManager,
			final ResourceManager resourceManager) {
		super(cores, rowsPerQuery, configFile, codecManager, resourceManager);
		Assertion.checkArgNotEmpty(serversNamesStr,
				"Il faut définir les urls des serveurs ElasticSearch (ex : host1:3889,host2:3889). Séparateur : ','");
		Assertion.checkArgument(!serversNamesStr.contains(";"),
				"Il faut définir les urls des serveurs ElasticSearch (ex : host1:3889,host2:3889). Séparateur : ','");
		Assertion.checkArgNotEmpty(clusterName, "Cluster's name must be defined");
		Assertion.checkArgument(!"elasticsearch".equals(clusterName),
				"You have to define a cluster name different from the default one");
		// ---------------------------------------------------------------------
		serversNames = serversNamesStr.split(",");
		this.clusterName = clusterName;
		this.nodeName = nodeName.isDefined() ? nodeName.get() : "es-embedded-node-" + System.currentTimeMillis();
	}

	/** {@inheritDoc} */
	@Override
	protected Client createClient() {
		client = new TransportClient(buildNodeSettings());
		for (final String serverName : serversNames) {
			final String[] serverNameSplit = serverName.split(":");
			Assertion.checkArgument(serverNameSplit.length == 2,
					"La déclaration du serveur doit être au format host:port ({0}", serverName);
			final int port = Integer.parseInt(serverNameSplit[1]);
			client.addTransportAddress(new InetSocketTransportAddress(serverNameSplit[0], port));
		}
		return client;
	}

	/** {@inheritDoc} */
	@Override
	protected void closeClient() {
		client.close();
	}

	private Settings buildNodeSettings() {
		// Build settings
		return ImmutableSettings.settingsBuilder().put("node.name", nodeName)
				// .put("node.data", false)
				// .put("node.master", false)
				// .put("discovery.zen.fd.ping_timeout", "30s")
				// .put("discovery.zen.minimum_master_nodes", 2)
				// .put("discovery.zen.ping.multicast.enabled", false)
				// .putArray("discovery.zen.ping.unicast.hosts", serversNames)
				.put("cluster.name", clusterName)
				// .put("index.store.type", "memory")
				// .put("index.store.fs.memory.enabled", "true")
				// .put("gateway.type", "none")
				.build();
	}

}
