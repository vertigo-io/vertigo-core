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
package io.vertigo.dynamo.collections.model;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

import java.io.Serializable;

/**
 * Valeur de facette relatif à une définition.
 * Les valeurs sont 
 *  - soit déclarée.  
 *  - soit déduite.
 * Exemple : 
 *  - Fourchettes de prix (valeurs déclarées)
 *  - Fourchettes de dates (valeurs déclarées)  
 *  - Termes les plus usités (valeurs déduites)
 *  - Clustering sémantique (valeurs déduites) 
 * Fait partie du métamodèle lorsque la facette est déclarée par ses ranges.
 * Fait partie du modèle lorsque les valeurs sont déduites. 
 * 
 * @author pchretien
 */
public final class FacetValue implements Serializable {
	private static final long serialVersionUID = -7077655936787603783L;
	private final MessageText label;
	private final ListFilter listFilter;

	/**
	 * Contructeur.
	 * @param listFilter Requete pour ce range
	 * @param label Label de cette facette
	 */
	public FacetValue(final ListFilter listFilter, final MessageText label) {
		Assertion.checkNotNull(listFilter);
		Assertion.checkNotNull(label);
		//---------------------------------------------------------------------
		this.listFilter = listFilter;
		this.label = label;
	}

	/**
	 * @return Label de la facette (exemples '1-2 ans' , '3-4 ans', '> 5 ans').
	 */
	public MessageText getLabel() {
		return label;
	}

	/**
	 * @return Méthode de filtrage de la liste.
	 */
	public ListFilter getListFilter() {
		return listFilter;
	}
}
