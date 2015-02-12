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
package io.vertigo.quarto.plugins.publisher.docx;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.lang.Assertion;
import io.vertigo.quarto.publisher.PublisherFormat;
import io.vertigo.quarto.publisher.impl.MergerPlugin;
import io.vertigo.quarto.publisher.impl.merger.grammar.ScriptGrammarUtil;
import io.vertigo.quarto.publisher.impl.merger.processor.GrammarEvaluatorProcessor;
import io.vertigo.quarto.publisher.impl.merger.processor.GrammarXMLBalancerProcessor;
import io.vertigo.quarto.publisher.impl.merger.processor.MergerProcessor;
import io.vertigo.quarto.publisher.impl.merger.processor.MergerScriptEvaluatorProcessor;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptGrammar;
import io.vertigo.quarto.publisher.model.PublisherData;
import io.vertigo.util.ListBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

/**
 * Gestionnaire des fusions de documents Docx.
 *
 * @author adufranne
 */
public final class DOCXMergerPlugin implements MergerPlugin {
	private final List<MergerProcessor> mergerProcessors;

	/**
	 * Constructeur avec ODTScriptGrammar par défaut.
	 *
	 * @param scriptManager le script manager.
	 */
	@Inject
	public DOCXMergerPlugin(final ScriptManager scriptManager) {
		Assertion.checkNotNull(scriptManager);
		//-----
		mergerProcessors = createMergerProcessors(scriptManager, ScriptGrammarUtil.createScriptGrammar());
	}

	private static List<MergerProcessor> createMergerProcessors(final ScriptManager scriptManager, final ScriptGrammar scriptGrammar) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(scriptGrammar);
		//-----
		return new ListBuilder<MergerProcessor>()
				// Extraction des variables.
				.add(new DOCXReverseInputProcessor())
				// équilibrage de l'arbre xml.
				.add(new GrammarXMLBalancerProcessor())
				// kscript <##> => jsp <%%>.
				.add(new GrammarEvaluatorProcessor(scriptManager, scriptGrammar))
				// Traitement Janino (TEXT balisé en java + Données => TEXT).
				.add(new MergerScriptEvaluatorProcessor(scriptManager, new DOCXValueEncoder()))
				// Post traitements (TEXT => XML(DOCX)).
				.add(new DOCXCleanerProcessor())
				.build();
	}

	/**
	 * Effectue la fusion.
	 *
	 * @return Fichier résultat de la fusion
	 * @throws IOException Exception système
	 */
	@Override
	public File execute(final URL modelFileURL, final PublisherData data) throws IOException {
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//-----
		final File file = DOCXUtil.obtainModelFile(modelFileURL);
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
	 * @param modelFile Fichier model, ce fichier n'est pas modifié.
	 * @param publisherData Parametres des données à fusionner
	 * @return Fichier d'entrée modifié par le sous-processor
	 */
	private File doExecute(final File modelFile, final PublisherData publisherData) throws IOException {
		try (final ZipFile docxFile = new ZipFile(modelFile)) {
			final Map<String, String> xmlContents = DOCXUtil.extractDOCXContents(docxFile);
			// Phase 1 : Exécution, chaque String est traitée dans l'ordre des processeurs.
			for (final MergerProcessor currentMergerProcessor : mergerProcessors) {
				// On passe le traitement sur les fichiers de Docx
				for (final Entry<String, String> xmlContent : xmlContents.entrySet()) {
					final String newXmlContent = currentMergerProcessor.execute(xmlContent.getValue(), publisherData);
					xmlContents.put(xmlContent.getKey(), newXmlContent);
				}
			}

			// Phase 2 : Reconstruction, le Fichier Docx est recomposé à partir des fichiers traités
			return DOCXUtil.createDOCX(docxFile, xmlContents);
		}
	}

	/** {@inheritDoc} */
	@Override
	public PublisherFormat getPublisherFormat() {
		return PublisherFormat.DOCX;
	}
}
