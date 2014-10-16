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
package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.core.definiton.Definition;
import io.vertigo.core.definiton.DefinitionPrefix;
import io.vertigo.core.lang.Assertion;

/**
 * Décrit une association entre deux objets (A et B)
 * L'association permet de décrire d'un point de vue conceptuel :
 * - les cardinalités, (notnull ; multiplicity )
 * - les roles,
 * - la navigation,
 * - les deux types d'objets ou DtDefinition ou classes mises en oeuvre.
 *
 * L'association permet aussi de décrire les choix d'implémentation effectués
 * - Foreign key
 * - Nom de table
 *
 *
 * 3 cas de figure :
 *  >>Relation simple     A-B = (0)1 -- (0)1
 *  >>Relation multpliple A-B = (0)1 -- *
 *  >>Relation complexe   A-B =   *  -- *
 *  Pour la relation simple on copie la clé de B dans A
 *  Pour la relation multiple on copie la relation de A dans B
 *  Pour la relation complexe XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author  jcassignol, pchretien
 */
@DefinitionPrefix("A")
public abstract class AssociationDefinition implements Definition {
	/**
	 * Nom de la définition.
	 */
	private final String name;
	private final AssociationNode associationNodeA;
	private final AssociationNode associationNodeB;

	/**
	 * Constructeur.
	 * @param associationNodeA Noeud A
	 * @param associationNodeB Noeud B
	 */
	AssociationDefinition(final String name, final AssociationNode associationNodeA, final AssociationNode associationNodeB) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(associationNodeA);
		Assertion.checkNotNull(associationNodeB);
		//----------------------------------------------------------------------
		this.name = name;
		this.associationNodeA = associationNodeA;
		this.associationNodeB = associationNodeB;
		associationNodeA.setAssociationDefinition(this);
		associationNodeB.setAssociationDefinition(this);
	}

	/**
	 * @return Si il s'agit d'une association simple
	 */
	public abstract boolean isAssociationSimpleDefinition();

	/**
	 * @return Association castée en simple
	 */
	public abstract AssociationSimpleDefinition castAsAssociationSimpleDefinition();

	/**
	 * @return Association castée en NN
	 */
	public abstract AssociationNNDefinition castAsAssociationNNDefinition();

	/**
	 * Noeud A de l'association.
	 * @return AssociationNode
	 */
	public final AssociationNode getAssociationNodeA() {
		return associationNodeA;
	}

	/**
	 * Noeud B de l'association.
	 * @return AssociationNode
	 */
	public final AssociationNode getAssociationNodeB() {
		return associationNodeB;
	}

	/** {@inheritDoc} */
	public final String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}
}
