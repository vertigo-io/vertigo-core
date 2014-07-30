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
package io.vertigo.dynamo.plugins.search.solr.embedded;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.plugins.search.solr.AbstractSolrSearchServicesPlugin;
import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

//Vérifier
/**
 * Gestion de la connexion au serveur Solr en mode embarqué.
 * 
 * @author pchretien
 */
public final class EmbeddedSolrSearchServicesPlugin extends AbstractSolrSearchServicesPlugin {
	/** url du serveur solr.  */
	private final URL solrHomeURL;
	/** Conteneur multi coeurs - c'est à dire multi index*/
	private final CoreContainer coreContainer;

	/**
	 * Constructeur
	 * @param solrHome URL du serveur SOLR
	 * @param cores Liste des indexes
	 * @param rowsPerQuery Nombre d'élément retourné par query
	 * @param codecManager Manager des codecs
	 * @param localeManager Manager des messages localisés
	 */
	@Inject
	public EmbeddedSolrSearchServicesPlugin(@Named("home") final String solrHome, @Named("cores") final String cores, @Named("rowsPerQuery") final int rowsPerQuery, final CodecManager codecManager, final LocaleManager localeManager, final ResourceManager resourceManager) {
		super(cores, rowsPerQuery, codecManager);
		Assertion.checkArgNotEmpty(solrHome);
		// ---------------------------------------------------------------------
		solrHomeURL = resourceManager.resolve(solrHome);
		coreContainer = createCoreContainer(solrHomeURL);
	}

	/** {@inheritDoc} */
	public void stop() {
		coreContainer.shutdown();
	}

	/** {@inheritDoc} */
	@Override
	protected SolrServer createSolrServer(final String core) {
		Assertion.checkArgument(coreContainer.getCoreNames().contains(core), "core {0} non reconnu lors du démarrage du container", core);
		return new EmbeddedSolrServer(coreContainer, core);
	}

	private static CoreContainer createCoreContainer(final URL solrHomeURL) {
		Assertion.checkNotNull(solrHomeURL);
		//---------------------------------------------------------------------
		final File home = new File(solrHomeURL.getFile());
		Assertion.checkArgument(home.exists() && home.isDirectory(), "Le SolrHome : {0} n''existe pas, ou n''est pas un répertoire.", home.getAbsolutePath());
		Assertion.checkArgument(home.canWrite(), "L''application n''a pas les droits d''écriture sur le SolrHome : {0}", home.getAbsolutePath());
		final File solrXml = new File(home, "solr.xml");
		final CoreContainer container;
		try {
			container = CoreContainer.createAndLoad(home.getAbsolutePath(), solrXml);
			//System.out.println("Solr Data In: " + home.getAbsolutePath() + "\\#CORE_NAME#\\data\\index\\");
			return container;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
