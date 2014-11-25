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
package io.vertigo.studio.mda;

import io.vertigo.lang.Builder;
import io.vertigo.util.StringUtil;

import java.io.File;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Résultat de la génération.
 *
 * @author pchretien
 */
public final class ResultBuilder implements Builder<Result> {
	private static final Logger LOGGER = Logger.getLogger(Result.class);

	/** Nombre de fichiers écrits . */
	private int writtenFiles;
	/** Nombre de fichiers en erreurs. */
	private int errorFiles;
	/** Nombre de fichiers identiques. */
	private int identicalFiles;

	//	/** Liste des fichiers en erreur. */
	//	private final List<File> fileErrorList = new ArrayList<File>();

	private final long start = System.currentTimeMillis();

	@Override
	public Result build() {
		return new Result() {

			/** {@inheritDoc} */
			@Override
			public void displayResultMessage(final PrintStream out) {
				final long duration = System.currentTimeMillis() - start;
				System.out.append(StringUtil.format("\nGénération de {0} fichiers, {1} fichiers identiques et {2} problemes en {3} ms", writtenFiles, identicalFiles, errorFiles, duration));
			}
		};
	}

	/**
	 * Notification de la génération d'un fichier (écrit sur disque).
	 * @param file Fichier généré
	 * @param success Si la génération a réussi 
	 */
	public void addFileWritten(final File file, final boolean success) {
		if (success) {
			writtenFiles++;
			LOGGER.trace("Fichier généré : " + file.getAbsolutePath());
		} else {
			errorFiles++;
			//Ajout d'un fichier en erreur.
			LOGGER.trace("Fichier en erreur : " + file.getAbsolutePath());
		}
	}

	/** 
	 * Le fichier est identique
	 * @param file Fichier généré
	 */
	public void addIdenticalFile(final File file) {
		identicalFiles++;
		LOGGER.trace("Fichier identique : " + file.getAbsolutePath());
	}
}
