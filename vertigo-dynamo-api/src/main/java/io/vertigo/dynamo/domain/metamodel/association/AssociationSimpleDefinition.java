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

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.lang.Assertion;

/**
 * Relation simple : 1-1 ou 1-n
 * Une relation simple est toujours asymétrique.
 *
 * @author  jcassignol, pchretien
 */
public final class AssociationSimpleDefinition extends AssociationDefinition {
	private final AssociationNode foreignAssociationNode;
	private final AssociationNode primaryAssociationNode;
	private final String fkFieldName;

	/**
	 * Constructeur.
	 * @param associationNodeA Noeud A de l'association
	 * @param associationNodeB Noeud B de l'association
	 * @param isAprimaryNode Qui représente la clé primaire dans la relation ?
	 */
	private AssociationSimpleDefinition(final String urn, final String fkFieldName, final AssociationNode associationNodeA, final AssociationNode associationNodeB, final boolean isAprimaryNode) {
		super(urn, associationNodeA, associationNodeB);

		Assertion.checkNotNull(fkFieldName);
		//-----
		if (isAprimaryNode) {
			primaryAssociationNode = getAssociationNodeA();
			foreignAssociationNode = getAssociationNodeB();
		} else {
			primaryAssociationNode = getAssociationNodeB();
			foreignAssociationNode = getAssociationNodeA();
		}
		this.fkFieldName = fkFieldName;
	}

	/**
	 *
	 * @param fkFieldName >> Peut être null, doit être non null si la clé primaire ne peut être utilisée comme clé étrangère est ambigu
	 * @param dtDefinitionA Définition de DT
	 * @param isANavigable boolean
	 * @param roleA String
	 * @param labelA String
	 * @param isAMultiple boolean
	 * @param isANotNull boolean
	 * @param dtDefinitionB Définition de DT
	 * @param isBNavigable boolean
	 * @param roleB String
	 * @param labelB String
	 * @param isBMultiple boolean
	 * @param isBNotNull boolean
	 * @return AssociationSimpleDefinitionStandard
	 */
	public static AssociationSimpleDefinition createAssociationSimpleDefinition(
			//
			final String urn, final String fkFieldName,
			final DtDefinition dtDefinitionA, final boolean isANavigable, final String roleA, final String labelA, final boolean isAMultiple, final boolean isANotNull,
			final DtDefinition dtDefinitionB, final boolean isBNavigable, final String roleB, final String labelB, final boolean isBMultiple, final boolean isBNotNull
			) {
		//On vérifie que l'on est bien dans le cas d'une relation simple.
		Assertion.checkArgument(!(isAMultiple && isBMultiple), " {0} ne gère pas les relations n-n", AssociationSimpleDefinition.class);
		Assertion.checkNotNull(fkFieldName);
		//-----
		// Qui représente la clé primaire dans la relation ?
		final boolean isAPrimaryNode = AssociationUtil.isAPrimaryNode(isAMultiple, isANotNull, isBMultiple, isBNotNull);
		//-----
		final AssociationNode associationNodeA = new AssociationNode(dtDefinitionA, isANavigable, roleA, labelA, isAMultiple, isANotNull);
		final AssociationNode associationNodeB = new AssociationNode(dtDefinitionB, isBNavigable, roleB, labelB, isBMultiple, isBNotNull);
		//		LOG.trace("Creation NodeA:" + associationNodeA);
		//		LOG.trace("Creation NodeB:" + associationNodeB);

		return new AssociationSimpleDefinition(urn, fkFieldName, associationNodeA, associationNodeB, isAPrimaryNode);
	}

	//==========================================================================
	/**
	 * @return Noeud Principal de la relation
	 */
	public AssociationNode getPrimaryAssociationNode() {
		return primaryAssociationNode;
	}

	/**
	 * @return Noeud référencé
	 */
	public AssociationNode getForeignAssociationNode() {
		return foreignAssociationNode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssociationSimpleDefinition() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public AssociationSimpleDefinition castAsAssociationSimpleDefinition() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AssociationNNDefinition castAsAssociationNNDefinition() {
		throw new IllegalAccessError("Il ne s'agit pas d'une relation NN");
	}

	/**
	 * @return Champ portant la clé étrangère
	 */
	public DtField getFKField() {
		return foreignAssociationNode.getDtDefinition().getField(fkFieldName);
	}
}
