package io.vertigo.dynamo.plugins.search.solr.embedded;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.plugins.search.solr.AbstractSolrSearchServicesPlugin;
import io.vertigo.kernel.exception.VRuntimeException;
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
	private CoreContainer container;

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
		super(cores.split(","), rowsPerQuery, codecManager);
		Assertion.checkArgNotEmpty(solrHome);
		Assertion.checkNotNull(cores);
		// ---------------------------------------------------------------------
		solrHomeURL = resourceManager.resolve(solrHome);
	}

	/** {@inheritDoc} */
	@Override
	protected void doStart() {
		//Création du conteneur multi core
		container = createCoreContainer(solrHomeURL);
	}

	/** {@inheritDoc} */
	@Override
	protected void doStop() {
		container.shutdown();
	}

	/** {@inheritDoc} */
	@Override
	protected SolrServer createSolrServer(final String core) {
		Assertion.checkArgument(container.getCoreNames().contains(core), "core {0} non reconnu lors du démarrage du container", core);
		return new EmbeddedSolrServer(container, core);
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
			throw new VRuntimeException(e);
		}
	}
}
