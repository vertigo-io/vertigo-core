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
package io.vertigo.dynamo.plugins.search.solr.commonshttp;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.plugins.search.solr.AbstractSolrSearchServicesPlugin;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * Gestion de la connexion au serveur Solr en mode HTTP.
 * 
 * @author dchallas
 */
public final class CommonsHttpSolrSearchServicesPlugin extends AbstractSolrSearchServicesPlugin {
	/** url du serveur solr. */
	private final String serverURL;

	/**
	 * Constructeur.
	 * @param serverURL URL du serevur SOLR (ex : "http://localhost:8983/solr")
	 * @param cores Liste des indexes
	 * @param rowsPerQuery Liste des indexes
	 * @param localeManager Manager des messages localisés
	 */
	@Inject
	public CommonsHttpSolrSearchServicesPlugin(@Named("solr.url") final String serverURL, @Named("cores") final String cores, @Named("rowsPerQuery") final int rowsPerQuery, final CodecManager codecManager, final LocaleManager localeManager) {
		super(cores, rowsPerQuery, codecManager);
		Assertion.checkNotNull(serverURL, "Il faut définir l'url du serveur Solr.");
		// ---------------------------------------------------------------------
		this.serverURL = serverURL;
	}

	/** {@inheritDoc} */
	@Override
	protected SolrServer createSolrServer(final String core) {
		return new HttpSolrServer(serverURL + '/' + core);
	}

	public void stop() {
		// nada
	}
}
