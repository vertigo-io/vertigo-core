package io.vertigo.dynamo.plugins.search.solr.commonshttp;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.dynamo.plugins.search.solr.AbstractSolrSearchServicesPlugin;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;


/**
 * Gestion de la connexion au serveur Solr en mode HTTP.
 * 
 * @author dchallas
 * @version $Id: CommonsHttpSolrSearchServicesPlugin.java,v 1.5 2014/01/24 18:00:03 pchretien Exp $
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
		super(cores.split(","), rowsPerQuery, codecManager);
		Assertion.checkNotNull(serverURL, "Il faut définir l'url du serveur Solr.");
		Assertion.checkNotNull(cores);
		// ---------------------------------------------------------------------
		this.serverURL = serverURL;
	}

	/** {@inheritDoc} */
	@Override
	protected void doStart() {
		//ras
	}

	/** {@inheritDoc} */
	@Override
	protected void doStop() {
		//ras
	}

	/** {@inheritDoc} */
	@Override
	protected SolrServer createSolrServer(final String core) {
		return new HttpSolrServer(serverURL + '/' + core);
	}
}
