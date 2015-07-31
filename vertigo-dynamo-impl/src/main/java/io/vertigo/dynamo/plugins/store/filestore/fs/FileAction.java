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
/**
 * 
 */
package io.vertigo.dynamo.plugins.store.filestore.fs;

/**
 * Interface d'action sur un fichier.
 * 
 * @author skerdudou
 */
interface FileAction {

	enum State {
		/** Etat d'initialisation */
		READY,
		/** Etat après l'action process() */
		PROCESSED,
		/** Etat après clean */
		END,
		/** Etat d'erreeur */
		ERROR
	}

	/**
	 * Effectue l'action demandée sur le fichier.
	 * 
	 * @throws Exception Si impossible
	 */
	void process() throws Exception;

	/**
	 * Supprime les fichiers temporaires, les informations d'annulation et vide l'action.
	 */
	void clean();

	/**
	 * récupère le chemin complet du fichier. Ceci est nécessaire afin de retirer des inserts sur ce fichier et qui
	 * serait inutile au commit.
	 * 
	 * @return chemin du fichier
	 */
	String getAbsolutePath();
}
