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
package io.vertigo.dynamo.collections.facet.metamodel;

import io.vertigo.dynamo.collections.facet.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.stereotype.Prefix;

import java.util.Collections;
import java.util.List;

/**
 * Définition de Facette.
 * Une facette porte sur un champ donné de l'index.
 * On distingue deux types de facettes.
 * - celles remontant les terms d'un champ
 * - celles remontant les valeurs d'une facette discrétisée par une liste de segments.
 *
 * Une facette 
 *  - est identifiés par un nom unique au sein de son index.
 *  - posséde un Titre. 
 *  
 * Exemple : 
 * Pour une liste d'articles, on créera des définitions de facette 
 *  - pour segmenter les prix, 
 *  	. 0-10€ 	
 *  	. 10-50€ 	
 *  	. >50€
 *  - pour donner les principaux fabricants, (facette de type 'term')
 *  - etc.. 
 *
 * @author pchretien
 */
@Prefix("FCT")
public final class FacetDefinition implements Definition {
	private final String name;
	private final DtField dtField;
	private final MessageText label;
	private final List<FacetValue> facetValues;
	private final boolean rangeFacet;

	/**
	 * Constructeur.
	 * @param dtField Champ de l'index facetté
	 * @param label Libellé de la facette
	 * @param facetValues Liste des segments pour les facettes segmentées 
	 * @param hasFacetValues Si facette segmentée
	 */
	private FacetDefinition(final String name, final DtField dtField, final MessageText label, final List<FacetValue> facetValues, final boolean hasFacetValues) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dtField);
		Assertion.checkNotNull(label);
		Assertion.checkNotNull(facetValues);
		Assertion.checkArgument(!hasFacetValues || !facetValues.isEmpty(), "Les FacetDefinition de type 'term' doivent fournir une liste des segments vide");
		Assertion.checkArgument(hasFacetValues || facetValues.isEmpty(), "Les FacetDefinition de type 'range' doivent fournir la liste des segments non vides (FacetValues)");
		//-----------------------------------------------------------------
		this.name = name;
		this.dtField = dtField;
		this.label = label;
		this.facetValues = Collections.unmodifiableList(facetValues);
		this.rangeFacet = hasFacetValues;
	}

	static FacetDefinition createFacetDefinitionByRange(final String name, final DtField dtField, final MessageText label, final List<FacetValue> facetRanges) {
		return new FacetDefinition(name, dtField, label, facetRanges, true);
	}

	public static FacetDefinition createFacetDefinitionByTerm(final String name, final DtField dtField, final MessageText label) {
		return new FacetDefinition(name, dtField, label, Collections.<FacetValue> emptyList(), false);
	}

	/**
	 * @return Libellé de la facette.
	 */
	public MessageText getLabel() {
		return label;
	}

	/**
	 * Ce champ est nécessairement inclus dans l'index.
	 * @return Champ sur lequel porte la facette
	 */
	public DtField getDtField() {
		return dtField;
	}

	/**
	 * @return Liste des sélections/range.
	 */
	public List<FacetValue> getFacetRanges() {
		Assertion.checkArgument(rangeFacet, "Cette facette ({0}) n'est pas segmentée.", getName());
		//---------------------------------------------------------------------
		return facetValues;
	}

	/**
	 * Identifie le mode la facette.
	 * @return Si la facette est de type Range.
	 */
	public boolean isRangeFacet() {
		return rangeFacet;
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
