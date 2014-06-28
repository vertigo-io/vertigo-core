package io.vertigo.publisher.plugins.docx;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

/**
 * Gestionnaire des fusions de documents Docx.
 * 
 * @author adufranne
 * @version $Id: MergerPluginDOCX.java,v 1.6 2014/02/27 10:37:48 pchretien Exp $
 */
public final class MergerPluginDOCX implements MergerPlugin {
	private final List<MergerProcessor> mergerProcessors;

	/**
	 * Constructeur avec ODTScriptGrammar par d�fault.
	 * 
	 * @param scriptManager le script manager.
	 */
	@Inject
	public MergerPluginDOCX(final ScriptManager scriptManager) {
		mergerProcessors = createMergerProcessors(scriptManager, ScriptGrammarUtil.createScriptGrammar());
	}

	// -------------------------------------------------------------------------
	private static List<MergerProcessor> createMergerProcessors(final ScriptManager scriptManager, final ScriptGrammar scriptGrammar) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(scriptGrammar);
		// ---------------------------------------------------------------------
		final List<MergerProcessor> localMergerProcessors = new ArrayList<>();

		// Extraction des variables.
		localMergerProcessors.add(new DOCXReverseInputProcessor());

		// �quilibrage de l'arbre xml.
		localMergerProcessors.add(new GrammarXMLBalancerProcessor());

		// kscript <##> => jsp <%%>.
		localMergerProcessors.add(new GrammarEvaluatorProcessor(scriptManager, scriptGrammar));

		// Traitement Janino (TEXT balis� en java + Donn�es => TEXT).
		localMergerProcessors.add(new MergerScriptEvaluatorProcessor(scriptManager, new DOCXValueEncoder()));

		// Post traitements (TEXT => XML(DOCX)).
		localMergerProcessors.add(new DOCXCleanerProcessor());
		return localMergerProcessors;
	}

	/**
	 * Effectue la fusion.
	 * 
	 * @return Fichier r�sultat de la fusion
	 * @throws IOException Exception syst�me
	 */
	public File execute(final URL modelFileURL, final PublisherData data) throws IOException {
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		// ---------------------------------------------------------------------
		final File file = new File(modelFileURL.getFile());
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
		try (final ZipFile docxFile = new ZipFile(modelFile)) {
			final Map<String, String> xmlContents = DOCXUtil.extractDOCXContents(docxFile);
			// Phase 1 : Ex�cution, chaque String est trait�e dans l'ordre des processeurs.
			for (final MergerProcessor currentMergerProcessor : mergerProcessors) {
				// On passe le traitement sur les fichiers de Docx
				for (final Entry<String, String> xmlContent : xmlContents.entrySet()) {
					final String newXmlContent = currentMergerProcessor.execute(xmlContent.getValue(), publisherData);
					xmlContents.put(xmlContent.getKey(), newXmlContent);
				}
			}

			// Phase 2 : Reconstruction, le Fichier Docx est recompos� � partir des fichiers trait�s
			return DOCXUtil.createDOCX(docxFile, xmlContents);
		}
	}

	/** {@inheritDoc} */
	public PublisherFormat getPublisherFormat() {
		return PublisherFormat.DOCX;
	}
}
