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

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.stereotype.Association;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestion centralisée des annotations sur les objets générés.
 *
 * @author pchretien
 */
class AnnotationWriter {

	/** Chaine d'indentation. */
	private static final String INDENT = "	";

	/**
	 * Ectiture des annotations sur une DT_DEFINITION.
	 *
	 * @param dtDefinition DtDefinition
	 * @return Liste des lignes de code java à ajouter.
	 */
	List<String> writeAnnotations(final DtDefinition dtDefinition) {
		final List<String> lines = new ArrayList<>();
		// Générations des annotations Dynamo
		final StringBuilder buffer;
		// if (!isComputed) {
		buffer = new StringBuilder("@DtDefinition");
		if (!dtDefinition.isPersistent()) {
			buffer.append("(persistent = false)");
		}
		lines.add(buffer.toString());
		return lines;
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD.
	 *
	 * @param dtField Champ de la DT_DEFINITION
	 * @param dtDefinition DT_DEFINITION
	 * @return Liste des lignes de code java à ajouter.
	 */
	List<String> writeAnnotations(final DtField dtField, final DtDefinition dtDefinition) {
		final List<String> lines = new ArrayList<>();
		final String fieldName = dtField.getName();
		// Générations des annotations Dynamo
		final StringBuilder buffer;
		// if (!isComputed) {
		buffer = new StringBuilder("@Field(");
		buffer.append("domain = \"" + dtField.getDomain().getName() + "\", ");
		if (dtField.getType() != DtField.FieldType.DATA) {
			// "DATA" est la valeur par défaut de type dans l'annotation Field
			buffer.append("type = \"" + dtField.getType() + "\", ");
		}
		// La propriété Not null est obligatoirement renseignée
		if (dtField.isNotNull()) {
			// false est la valeur par défaut de notNull dans l'annotation Field
			buffer.append("notNull = true, ");
		}
		if (!dtField.isPersistent()) {
			// On ne précise la persistance que si elle n'est pas gérée
			buffer.append("persistent = false, ");
		}
		// On vérifie que le nom du champ (constante) est transformable en nom de méthode et réciproquement.
		Assertion.checkArgument(fieldName.equals(StringUtil.camelToConstCase(StringUtil.constToCamelCase(fieldName, true))), "le nom {0} n''est pas transformable en nom de méthode", fieldName);
		buffer.append("label = \"" + dtField.getLabel().getDisplay() + "\"");
		// on place le label a la fin, car il ne faut pas de ','
		buffer.append(")");
		lines.add(buffer.toString());
		return lines;
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD gérant une association.
	 *
	 * @param associationNode Noeud de l'association
	 * @return Liste des lignes de code java à ajouter.
	 */
	List<String> writeAnnotations(final AssociationNode associationNode) {
		final List<String> lines = new ArrayList<>();
		// Générations des annotations Dynamo
		if (associationNode.getAssociationDefinition().isAssociationSimpleDefinition()) {
			final AssociationSimpleDefinition associationSimple = associationNode.getAssociationDefinition().castAsAssociationSimpleDefinition();
			final AssociationNode primaryNode = associationSimple.getPrimaryAssociationNode();
			final AssociationNode foreignNode = associationSimple.getForeignAssociationNode();
			lines.add("@" + Association.class.getCanonicalName() + " (");
			lines.add(INDENT + "name = \"" + associationSimple.getName() + "\",");
			lines.add(INDENT + "fkFieldName = \"" + associationSimple.getFKField().getName() + "\",");
			lines.add(INDENT + "primaryDtDefinitionName = \"" + primaryNode.getDtDefinition().getName() + "\",");
			lines.add(INDENT + "primaryIsNavigable = " + primaryNode.isNavigable() + ',');
			final String primaryMultiplicity = AssociationUtil.getMultiplicity(primaryNode.isNotNull(), primaryNode.isMultiple());
			lines.add(INDENT + "primaryRole = \"" + primaryNode.getRole() + "\",");
			lines.add(INDENT + "primaryLabel = \"" + primaryNode.getLabel() + "\",");
			lines.add(INDENT + "primaryMultiplicity = \"" + primaryMultiplicity + "\",");
			final String foreignMultiplipicity = AssociationUtil.getMultiplicity(foreignNode.isNotNull(), foreignNode.isMultiple());
			lines.add(INDENT + "foreignDtDefinitionName = \"" + foreignNode.getDtDefinition().getName() + "\",");
			lines.add(INDENT + "foreignIsNavigable = " + foreignNode.isNavigable() + ',');
			lines.add(INDENT + "foreignRole = \"" + foreignNode.getRole() + "\",");
			lines.add(INDENT + "foreignLabel = \"" + foreignNode.getLabel() + "\",");
			lines.add(INDENT + "foreignMultiplicity = \"" + foreignMultiplipicity + "\"");
			lines.add(")");
		} else {
			final AssociationNNDefinition associationNN = associationNode.getAssociationDefinition().castAsAssociationNNDefinition();
			final AssociationNode nodeA = associationNN.getAssociationNodeA();
			final AssociationNode nodeB = associationNN.getAssociationNodeB();
			lines.add("@io.vertigo.dynamo.domain.metamodel.annotation.AssociationNN (");
			lines.add(INDENT + "name = \"" + associationNN.getName() + "\",");
			lines.add(INDENT + "tableName = \"" + associationNN.getTableName() + "\",");
			lines.add(INDENT + "dtDefinitionA = \"" + nodeA.getDtDefinition().getName() + "\",");
			lines.add(INDENT + "dtDefinitionB = \"" + nodeB.getDtDefinition().getName() + "\",");
			lines.add(INDENT + "navigabilityA = " + nodeA.isNavigable() + ',');
			lines.add(INDENT + "navigabilityB = " + nodeB.isNavigable() + ',');
			lines.add(INDENT + "roleA = \"" + nodeA.getRole() + "\",");
			lines.add(INDENT + "roleB = \"" + nodeB.getRole() + "\",");
			lines.add(INDENT + "labelA = \"" + nodeA.getLabel() + "\",");
			lines.add(INDENT + "labelB = \"" + nodeB.getLabel() + "\"");
			lines.add(")");
		}
		return lines;
	}
}
