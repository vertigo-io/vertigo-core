package io.vertigo.publisher.plugins.odt;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.publisher.PublisherFormat;
import io.vertigo.publisher.impl.MergerPlugin;
import io.vertigo.publisher.impl.merger.grammar.ScriptGrammarUtil;
import io.vertigo.publisher.impl.merger.processor.GrammarEvaluatorProcessor;
import io.vertigo.publisher.impl.merger.processor.GrammarXMLBalancerProcessor;
import io.vertigo.publisher.impl.merger.processor.MergerProcessor;
import io.vertigo.publisher.impl.merger.processor.MergerScriptEvaluatorProcessor;
import io.vertigo.publisher.impl.merger.script.ScriptGrammar;
import io.vertigo.publisher.model.PublisherData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import javax.inject.Inject;

/**
 * Gestionnaire des fusions de documents OpenOffice.
 * 
 * @author npiedeloup
 * @version $Id: MergerPluginOpenOffice.java,v 1.7 2014/02/27 10:39:42 pchretien Exp $
 */
public final class MergerPluginOpenOffice implements MergerPlugin {
	private final List<MergerProcessor> mergerProcessors;

	/**
	 * Constructeur avec ODTScriptGrammar par d�fault.
	 * @param scriptManager le script manager.
	 */
	@Inject
	public MergerPluginOpenOffice(final ScriptManager scriptManager) {
		mergerProcessors = createMergerProcessors(scriptManager, ScriptGrammarUtil.createScriptGrammar());
	}

	//-------------------------------------------------------------------------
	private static List<MergerProcessor> createMergerProcessors(final ScriptManager scriptManager, final ScriptGrammar scriptGrammar) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(scriptGrammar);
		//---------------------------------------------------------------------
		final List<MergerProcessor> localMergerProcessors = new ArrayList<>();

		// Pr�Traitement (TEXT balis� grammaire simplifi�e => TEXT balis� en  java)
		// 	Permet de red�finir les pr�traitements en - ajoutant une grammaire de
		//  haut niveau - modifiant la pr�sentation OpenOfice - etc..

		localMergerProcessors.add(new ODTReverseInputProcessor());
		localMergerProcessors.add(new GrammarXMLBalancerProcessor());
		localMergerProcessors.add(new GrammarEvaluatorProcessor(scriptManager, scriptGrammar));

		// Traitement Janino (TEXT balis� en java + Donn�es => TEXT)
		localMergerProcessors.add(new MergerScriptEvaluatorProcessor(scriptManager, new ODTValueEncoder()));

		// Post traitements (TEXT => XML(ODT))
		localMergerProcessors.add(new ODTCleanerProcessor());
		return localMergerProcessors;
	}

	/** {@inheritDoc} */
	public File execute(final URL modelFileURL, final PublisherData data) throws IOException {
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		final File file = new File(modelFileURL.getFile());
		Assertion.checkArgument(file.exists(), "Le fichier du mod�le est introuvable");
		Assertion.checkArgument(file.canRead(), "Le fichier du mod�le n'est pas lisible");
		file.setReadOnly();
		try {
			return doExecute(file, data);
		} finally {
			file.setWritable(true);
		}
	}

	/**
	 * Effectue le traitement.
	 * 
	 * @param modelFile Fichier model, ce fichier n'est pas modifi�.
	 * @param publisherData Parametres des donn�es � fusionner 
	 * @return Fichier d'entr�e modifi� par le sous-processor
	 */
	private File doExecute(final File modelFile, final PublisherData publisherData) throws IOException {
		// Phase 1 : D�coupage, le Fichier ODT est scind� en deux String
		// (XmlContent, et XmlStyles)
		try (final ZipFile odtFile = new ZipFile(modelFile)) {

			String newXmlContent = ODTUtil.extractContent(odtFile);
			String newXmlStyles = ODTUtil.extractStyles(odtFile);

			// Phase 1 : extraction du mapping des images � remplacer
			final ODTImageProcessor imageProcessor = new ODTImageProcessor(); // pas dans la liste des preprocessus, car celui-ci est STATE_FULL
			newXmlContent = imageProcessor.execute(newXmlContent, publisherData);
			newXmlStyles = imageProcessor.execute(newXmlStyles, publisherData);

			// Phase 2 : Ex�cution, chaque String est trait�e dans l'ordre des processeurs.
			for (final MergerProcessor currentMergerProcessor : mergerProcessors) {
				// On passe le traitement sur les fichiers Content et Styles de l'odt
				newXmlContent = currentMergerProcessor.execute(newXmlContent, publisherData);
				newXmlStyles = currentMergerProcessor.execute(newXmlStyles, publisherData);
			}

			// Phase 3 : Reconstruction, le Fichier ODT est recompos� � partir
			// des deux fichiers trait�s (XmlContent, et XmlStyles), et remplacement des images
			return ODTUtil.createODT(odtFile, newXmlContent, newXmlStyles, imageProcessor.getNewImageMap());
		}
	}

	/** {@inheritDoc} */
	public PublisherFormat getPublisherFormat() {
		return PublisherFormat.ODT;
	}
}
