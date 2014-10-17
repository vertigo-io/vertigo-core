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
package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.lang.Assertion;

/**
 * Objet utilisé par FreeMarker.
 * 
 * @author pchretien
 */
public final class TemplateAssociation {
	private final AssociationNode associationNode;

	/**
	 * Constructeur.
	 * @param associationNode Noeud de l'association à générer
	 */
	TemplateAssociation(final AssociationNode associationNode) {
		Assertion.checkNotNull(associationNode);
		//-----------------------------------------------------------------
		this.associationNode = associationNode;
	}

	/**
	 * @return Noeud d'une association.
	 */
	public AssociationNode getAssociationNode() {
		return associationNode;
	}

	/**
	 * @return Label du noeud
	 */
	public String getLabel() {
		return associationNode.getLabel();
	}

	/**
	 * @return Role du noeud
	 */
	public String getRole() {
		return associationNode.getRole();
	}

	/**
	 * @return Si la cardinalité max du noeud est multiple
	 */
	public boolean isMultiple() {
		return associationNode.isMultiple();
	}

	/**
	 * @return Si le noeud est navigable
	 */
	public boolean isNavigable() {
		return associationNode.isNavigable();
	}

	/**
	 * @return Type à retourner
	 */
	public String getReturnType() {
		return associationNode.getDtDefinition().getClassCanonicalName();
	}

	/**
	 * @return Urn de la définition de l'association
	 */
	public String getUrn() {
		return associationNode.getAssociationDefinition().getName();
	}
}
