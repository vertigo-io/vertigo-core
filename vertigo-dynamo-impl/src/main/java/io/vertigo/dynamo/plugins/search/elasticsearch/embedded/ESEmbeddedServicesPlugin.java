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
package io.vertigo.dynamo.plugins.search.elasticsearch.embedded;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.plugins.search.elasticsearch.AbstractESServicesPlugin;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

//Vérifier
/**
 * Gestion de la connexion au serveur ElasticSearch en mode embarqué.
 *
 * @author pchretien, npiedeloup
 */
public final class ESEmbeddedServicesPlugin extends AbstractESServicesPlugin {
	/** url du serveur elasticSearch.  */
	private final URL elasticSearchHomeURL;

	private final Node node;

	/**
	 * Constructeur
	 * @param elasticSearchHome URL du serveur SOLR
	 * @param cores Liste des indexes
	 * @param rowsPerQuery Nombre d'élément retourné par query
	 * @param codecManager Manager des codecs
	 * @param localeManager Manager des messages localisés
	 * @param resourceManager Manager d'accès aux ressources
	 */
	@Inject
	public ESEmbeddedServicesPlugin(@Named("home") final String elasticSearchHome, @Named("cores") final String cores, @Named("rowsPerQuery") final int rowsPerQuery, final CodecManager codecManager, final ResourceManager resourceManager) {
		super(cores, rowsPerQuery, codecManager);
		Assertion.checkArgNotEmpty(elasticSearchHome);
		// ---------------------------------------------------------------------
		elasticSearchHomeURL = resourceManager.resolve(elasticSearchHome);
		node = createNode(elasticSearchHomeURL);
		node.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		node.close();
	}

	/** {@inheritDoc} */
	@Override
	protected Client createEsClient() {
		return node.client();
	}

	private static Node createNode(final URL esHomeURL) {
		Assertion.checkNotNull(esHomeURL);
		//---------------------------------------------------------------------
		final File home = new File(esHomeURL.getFile());
		Assertion.checkArgument(home.exists() && home.isDirectory(), "Le ElasticSearchHome : {0} n''existe pas, ou n''est pas un répertoire.", home.getAbsolutePath());
		Assertion.checkArgument(home.canWrite(), "L''application n''a pas les droits d''écriture sur le ElasticSearchHome : {0}", home.getAbsolutePath());

		return new NodeBuilder() //
		.settings(buildNodeSettings(home.getAbsolutePath()))//
		.local(true) //
				.build();
	}

	private static Settings buildNodeSettings(final String homePath) {
		//Build settings
		return ImmutableSettings.settingsBuilder() //
				.put("node.name", "es-embedded-node-" + System.currentTimeMillis())
				// .put("node.data", true)
				// .put("cluster.name", "cluster-test-" + NetworkUtils.getLocalAddress().getHostName())
				//.put("index.store.type", "memory")//
				//.put("index.store.fs.memory.enabled", "true")//
				//.put("gateway.type", "none")//
				.put("path.home", homePath)//
				.build();
	}
}
