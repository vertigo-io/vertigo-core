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
package io.vertigo.studio.plugins.mda;

import io.vertigo.lang.Assertion;

import java.util.Properties;

/**
 * Configuration du generateur de fichiers.
 *
 * @author dchallas
 */
public final class FileConfiguration {
	/**
	 * Répertoire des fichiers TOUJOURS générés
	 * Doit être renseigné dans le fichier properties [targetDir]
	 */
	private final String targetGenDir;
	/**
	 * Racine du projet.
	 */
	private final String projectPackageName;
	/**
	 * Encoding des fichiers générés.
	 */
	private final String encoding;

	private final String suffix;

	/**
	 * Chargement des paramètres depuis le fichier properties.
	 *
	 * @param properties Paramètres de la génération
	 */
	public FileConfiguration(final Properties properties, final String suffix) {
		Assertion.checkNotNull(properties);
		Assertion.checkNotNull(suffix);
		// ---------------------------------------------------------------------
		this.suffix = suffix;
		targetGenDir = getPropertyNotNull(properties, "targetGenDir", "Le repertoire des fichiers generes [targetGenDir] doit etre renseigné !");
		Assertion.checkState(targetGenDir.endsWith("/"), "Le chemin doit finir par '/'.");
		projectPackageName = getPropertyNotNull(properties, "project.packagename", "le package racine du projet doit être renseigne ! [project.packagename]");
		encoding = getPropertyNotNull(properties, "encoding", "l'encoding des fichiers gérénés [encoding] doit etre renseigné !");
	}

	/**
	 * Retourne une propriété non null.
	 * @param properties Propriétés
	 * @param propertyName Nom de la propriété recherchée
	 * @param messageIfNull Message en cas de propriété non trouvée
	 * @return Valeur de la propriété
	 */
	private static String getPropertyNotNull(final Properties properties, final String propertyName, final String messageIfNull) {
		Assertion.checkNotNull(properties);
		Assertion.checkNotNull(propertyName);
		//---------------------------------------------------------------------
		final String property = properties.getProperty(propertyName, null);
		Assertion.checkNotNull(property, messageIfNull);
		return property.trim();
	}

	public String getPackageName() {
		return getProjectPackageName() + "." + suffix;
	}

	/**
	 * Donne la valeur de projectPackageName.
	 *
	 * @return la valeur de projectPackageName.
	 */
	public String getProjectPackageName() {
		return projectPackageName;
	}

	/**
	 * Donne la valeur de targetGenDir.
	 *
	 * @return la valeur de targetGenDir.
	 */
	public String getTargetGenDir() {
		return targetGenDir;
	}

	/**
	 * Donne la valeur de encoding.
	 *
	 * @return la valeur de encoding.
	 */
	public String getEncoding() {
		return encoding;
	}
}
