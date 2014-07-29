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
package io.vertigo.quarto.publisher.mock;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Attention cette classe est g�n�r�e automatiquement !
 * Objet de donn�es AbstractEnqueteur
 */
@io.vertigo.dynamo.domain.stereotype.DtDefinition(persistent = false)
public final class Enqueteur implements DtObject {
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@io.vertigo.dynamo.domain.stereotype.Field(domain = "DO_STRING", label = "Nom")
	private String nom;
	@io.vertigo.dynamo.domain.stereotype.Field(domain = "DO_STRING", label = "Prenom")
	private String prenom;

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Nom'. 
	 * @return String nom 
	 */
	public final String getNom() {
		return nom;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Nom'.
	 * @param nom String 
	 */
	public final void setNom(final String nom) {
		this.nom = nom;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Prenom'. 
	 * @return String prenom 
	 */
	public final String getPrenom() {
		return prenom;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Prenom'.
	 * @param prenom String 
	 */
	public final void setPrenom(final String prenom) {
		this.prenom = prenom;
	}
}