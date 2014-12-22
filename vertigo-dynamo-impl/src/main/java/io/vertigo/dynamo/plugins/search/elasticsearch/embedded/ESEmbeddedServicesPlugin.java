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
import io.vertigo.dynamo.plugins.search.elasticsearch.AbstractESServicesPlugin;
import io.vertigo.lang.Assertion;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.inject.Inject;
import javax.inject.Named;

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

	/**
	 * Constructeur
	 * @param elasticSearchHome URL du serveur SOLR
	 * @param cores Liste des indexes
	 * @param rowsPerQuery Nombre d'élément retourné par query
	 * @param codecManager Manager des codecs
	 * @param resourceManager Manager d'accès aux ressources
	 */
	@Inject
	public ESEmbeddedServicesPlugin(@Named("home") final String elasticSearchHome, @Named("cores") final String cores, @Named("rowsPerQuery") final int rowsPerQuery, final CodecManager codecManager, final ResourceManager resourceManager) {
		super(cores, rowsPerQuery, codecManager);
		Assertion.checkArgNotEmpty(elasticSearchHome);
		//-----
		elasticSearchHomeURL = resourceManager.resolve(elasticSearchHome);
	}

	/** {@inheritDoc} */
	@Override
	protected Node createNode() {
		return createNode(elasticSearchHomeURL);
	}

	private static Node createNode(final URL esHomeURL) {
		Assertion.checkNotNull(esHomeURL);
		//-----
		final File home;
		try {
			home = new File(URLDecoder.decode(esHomeURL.getFile(), "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Error de parametrage du ElasticSearchHome " + esHomeURL, e);
		}
		Assertion.checkArgument(home.exists() && home.isDirectory(), "Le ElasticSearchHome : {0} n''existe pas, ou n''est pas un répertoire.", home.getAbsolutePath());
		Assertion.checkArgument(home.canWrite(), "L''application n''a pas les droits d''écriture sur le ElasticSearchHome : {0}", home.getAbsolutePath());
		return new NodeBuilder()
				.settings(buildNodeSettings(home.getAbsolutePath()))
				.local(true)
				.build();
	}

	private static Settings buildNodeSettings(final String homePath) {
		//Build settings
		return ImmutableSettings.settingsBuilder()
				.put("node.name", "es-embedded-node-" + System.currentTimeMillis())
				.put("path.home", homePath)
				.build();
	}
}
