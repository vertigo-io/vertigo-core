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
package io.vertigo.quarto.plugins.publisher.odt;

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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import javax.inject.Inject;

/**
 * Gestionnaire des fusions de documents OpenOffice.
 *
 * @author npiedeloup
 */
public final class OpenOfficeMergerPlugin implements MergerPlugin {
	private final List<MergerProcessor> mergerProcessors;

	/**
	 * Constructeur avec ODTScriptGrammar par défaut.
	 * @param scriptManager le script manager.
	 */
	@Inject
	public OpenOfficeMergerPlugin(final ScriptManager scriptManager) {
		mergerProcessors = createMergerProcessors(scriptManager, ScriptGrammarUtil.createScriptGrammar());
	}

	private static List<MergerProcessor> createMergerProcessors(final ScriptManager scriptManager, final ScriptGrammar scriptGrammar) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(scriptGrammar);
		//-----
		final List<MergerProcessor> localMergerProcessors = new ArrayList<>();

		// Pré-Traitement (TEXT balisé grammaire simplifiée => TEXT balisé en  java)
		// 	Permet de redéfinir les prétraitements en - ajoutant une grammaire de
		//  haut niveau - modifiant la présentation OpenOfice - etc..

		localMergerProcessors.add(new ODTReverseInputProcessor());
		localMergerProcessors.add(new GrammarXMLBalancerProcessor());
		localMergerProcessors.add(new GrammarEvaluatorProcessor(scriptManager, scriptGrammar));

		// Traitement Janino (TEXT balisé en java + Données => TEXT)
		localMergerProcessors.add(new MergerScriptEvaluatorProcessor(scriptManager, new ODTValueEncoder()));

		// Post traitements (TEXT => XML(ODT))
		localMergerProcessors.add(new ODTCleanerProcessor());
		return localMergerProcessors;
	}

	/** {@inheritDoc} */
	@Override
	public File execute(final URL modelFileURL, final PublisherData data) throws IOException {
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//-----
		File file;
		try {
			file = new File(modelFileURL.toURI());
		} catch (final URISyntaxException e) {
			throw new IOException("Model URL invalid", e);
		}
		Assertion.checkArgument(file.exists(), "Le fichier du modèle est introuvable");
		Assertion.checkArgument(file.canRead(), "Le fichier du modèle n'est pas lisible");
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
		// Phase 1 : Découpage, le Fichier ODT est scindé en deux String
		// (XmlContent, et XmlStyles)
		try (final ZipFile odtFile = new ZipFile(modelFile)) {

			String newXmlContent = ODTUtil.extractContent(odtFile);
			String newXmlStyles = ODTUtil.extractStyles(odtFile);

			// Phase 1 : extraction du mapping des images à remplacer
			final ODTImageProcessor imageProcessor = new ODTImageProcessor(); // pas dans la liste des preprocessus, car celui-ci est STATE_FULL
			newXmlContent = imageProcessor.execute(newXmlContent, publisherData);
			newXmlStyles = imageProcessor.execute(newXmlStyles, publisherData);

			// Phase 2 : Exécution, chaque String est traitée dans l'ordre des processeurs.
			for (final MergerProcessor currentMergerProcessor : mergerProcessors) {
				// On passe le traitement sur les fichiers Content et Styles de l'odt
				newXmlContent = currentMergerProcessor.execute(newXmlContent, publisherData);
				newXmlStyles = currentMergerProcessor.execute(newXmlStyles, publisherData);
			}

			// Phase 3 : Reconstruction, le Fichier ODT est recomposé à partir
			// des deux fichiers traités (XmlContent, et XmlStyles), et remplacement des images
			return ODTUtil.createODT(odtFile, newXmlContent, newXmlStyles, imageProcessor.getNewImageMap());
		}
	}

	/** {@inheritDoc} */
	@Override
	public PublisherFormat getPublisherFormat() {
		return PublisherFormat.ODT;
	}
}
