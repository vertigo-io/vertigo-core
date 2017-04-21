/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.io.File;

import org.apache.log4j.Logger;

import io.vertigo.lang.Builder;

/**
 * Résultat de la génération.
 *
 * @author pchretien
 */
public final class MdaResultBuilder implements Builder<MdaResult> {
	private static final Logger LOGGER = Logger.getLogger(MdaResult.class);

	/** Nombre de fichiers écrits . */
	private int updatedFiles;
	private int createdFiles;
	private int deletedFiles;
	/** Nombre de fichiers en erreurs. */
	private int errorFiles;
	/** Nombre de fichiers identiques. */
	private int identicalFiles;

	private final long start = System.currentTimeMillis();

	@Override
	public MdaResult build() {
		final long duration = System.currentTimeMillis() - start;
		return new MdaResult(createdFiles, updatedFiles, errorFiles, identicalFiles, deletedFiles, duration);
	}

	public void incFileDeleted() {
		deletedFiles++;
	}

	/**
	 * Notification de la génération d'un fichier (écrit sur disque).
	 * @param file Fichier généré
	 */
	public void addCreatedFile(final File file) {
		createdFiles++;
		LOGGER.trace("file created : " + file.getAbsolutePath());
	}

	public void addErrorFile(final File file) {
		errorFiles++;
		//Ajout d'un fichier en erreur.
		LOGGER.trace("file error : " + file.getAbsolutePath());
	}

	public void addUpdatedFile(final File file) {
		updatedFiles++;
		LOGGER.trace("file updated : " + file.getAbsolutePath());
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
