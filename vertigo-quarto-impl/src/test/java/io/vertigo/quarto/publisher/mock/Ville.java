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
 * Objet de donn�es AbstractVille
 */
@io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition(persistent = false)
public final class Ville implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Nom")
	private String nom;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Code postal")
	private String codePostal;

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
	 * R�cup�re la valeur de la propri�t� 'Code postal'. 
	 * @return String codePostal 
	 */
	public final String getCodePostal() {
		return codePostal;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Code postal'.
	 * @param codePostal String 
	 */
	public final void setCodePostal(final String codePostal) {
		this.codePostal = codePostal;
	}
}