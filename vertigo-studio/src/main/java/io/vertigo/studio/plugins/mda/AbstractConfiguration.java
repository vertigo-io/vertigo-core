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

import static io.vertigo.studio.impl.mda.PropertiesUtil.getPropertyNotNull;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.mda.Configuration;

import java.util.Properties;

/**
 * Configuration du generateur de fichiers. 
 * 
 * @author dchallas
 */
public class AbstractConfiguration implements Configuration {
	/**
	 * Répertoire des fichiers générés une fois.
	 * Doit être renseigné dans le fichier properties [targetDir]
	 */
	private final String targetDir;

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
	 * Chargement des paramètres depuis le fichier properties.
	 * @param properties Paramètres de la génération
	 */
	protected AbstractConfiguration(final Properties properties) {
		Assertion.checkNotNull(properties);
		//---------------------------------------------------------------------
		targetDir = getPropertyNotNull(properties, "targetDir", "Le repertoire des fichiers generes [targetDir] doit etre renseigne !");
		Assertion.checkState(targetDir.endsWith("/"), "Le chemin doit finir par '/'.");

		targetGenDir = getPropertyNotNull(properties, "targetGenDir", "Le repertoire des fichiers generes [targetGenDir] doit etre renseigne !");
		Assertion.checkState(targetGenDir.endsWith("/"), "Le chemin doit finir par '/'.");

		projectPackageName = getPropertyNotNull(properties, "project.packagename", "le package racine du projet doit être renseigne ! [project.packagename]");
	}

	public final String getProjectPackageName() {
		return projectPackageName;
	}

	final String getTargetDir() {
		return targetDir;
	}

	final String getTargetGenDir() {
		return targetGenDir;
	}
}
