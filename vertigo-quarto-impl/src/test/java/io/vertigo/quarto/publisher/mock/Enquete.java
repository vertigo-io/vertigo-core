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
 * Objet de donn�es AbstractEnquete
 */
@io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition(persistent = false)
public final class Enquete implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_BOOLEAN", label = "Termin�e?")
	private Boolean enqueteTerminee;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Code")
	private String codeEnquete;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Sexe")
	private String fait;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_BOOLEAN", label = "Sexe")
	private Boolean siGrave;

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Termin�e?'. 
	 * @return Boolean enqueteTerminee 
	 */
	public final Boolean getEnqueteTerminee() {
		return enqueteTerminee;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Termin�e?'.
	 * @param enqueteTerminee Boolean 
	 */
	public final void setEnqueteTerminee(final Boolean enqueteTerminee) {
		this.enqueteTerminee = enqueteTerminee;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Code'. 
	 * @return String codeEnquete 
	 */
	public final String getCodeEnquete() {
		return codeEnquete;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Code'.
	 * @param codeEnquete String 
	 */
	public final void setCodeEnquete(final String codeEnquete) {
		this.codeEnquete = codeEnquete;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Sexe'. 
	 * @return String fait 
	 */
	public final String getFait() {
		return fait;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Sexe'.
	 * @param fait String 
	 */
	public final void setFait(final String fait) {
		this.fait = fait;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Sexe'. 
	 * @return Boolean siGrave 
	 */
	public final Boolean getSiGrave() {
		return siGrave;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Sexe'.
	 * @param siGrave Boolean 
	 */
	public final void setSiGrave(final Boolean siGrave) {
		this.siGrave = siGrave;
	}
}