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
 * Objet de donn�es AbstractAddress
 */
@io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition(persistent = false)
public final class Address implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Rue")
	private String rue;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_DT_VILLE_DTO", label = "Ville")
	private io.vertigo.quarto.publisher.mock.Ville ville;

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Rue'. 
	 * @return String rue 
	 */
	public final String getRue() {
		return rue;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Rue'.
	 * @param rue String 
	 */
	public final void setRue(final String rue) {
		this.rue = rue;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Ville'. 
	 * @return io.vertigo.publisher.mock.Ville ville 
	 */
	public final io.vertigo.quarto.publisher.mock.Ville getVille() {
		return ville;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Ville'.
	 * @param ville io.vertigo.publisher.mock.Ville 
	 */
	public final void setVille(final io.vertigo.quarto.publisher.mock.Ville ville) {
		this.ville = ville;
	}
}
