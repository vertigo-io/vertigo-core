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
package io.vertigo.studio.plugins.mda.domain.templates;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.stereotype.Association;
import io.vertigo.dynamo.domain.stereotype.AssociationNN;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.StringUtil;

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
		// if (!isComputed) {
		final StringBuilder buffer = new StringBuilder("@DtDefinition");
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
		// if (!isComputed) {
		final StringBuilder buffer = new StringBuilder("@Field(")
				.append("domain = \"" + dtField.getDomain().getName() + "\", ");
		if (dtField.getType() != DtField.FieldType.DATA) {
			// "DATA" est la valeur par défaut de type dans l'annotation Field
			buffer.append("type = \"" + dtField.getType() + "\", ");
		}
		// La propriété Not null est obligatoirement renseignée
		if (dtField.isRequired()) {
			// false est la valeur par défaut de notNull dans l'annotation Field
			buffer.append("notNull = true, ");
		}
		if (!dtField.isPersistent()) {
			// On ne précise la persistance que si elle n'est pas gérée
			buffer.append("persistent = false, ");
		}
		// On vérifie que le nom du champ (constante) est transformable en nom de méthode et réciproquement.
		Assertion.checkArgument(fieldName.equals(StringUtil.camelToConstCase(StringUtil.constToUpperCamelCase(fieldName))), "le nom {0} n''est pas transformable en nom de méthode", fieldName);

		buffer
				.append("label = \"" + dtField.getLabel().getDisplay() + "\"")
				// on place le label a la fin, car il ne faut pas de ','
				.append(")");
		lines.add(buffer.toString());
		return lines;
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD gérant une association.
	 *
	 * @param associationSimple Definition de l'association
	 * @return Liste des lignes de code java à ajouter.
	 */
	List<String> writeSimpleAssociationAnnotation(final AssociationSimpleDefinition associationSimple) {
		final AssociationNode primaryNode = associationSimple.getPrimaryAssociationNode();
		final AssociationNode foreignNode = associationSimple.getForeignAssociationNode();
		final String primaryMultiplicity = AssociationUtil.getMultiplicity(primaryNode.isNotNull(), primaryNode.isMultiple());
		final String foreignMultiplipicity = AssociationUtil.getMultiplicity(foreignNode.isNotNull(), foreignNode.isMultiple());

		return new ListBuilder<String>()
				.add("@" + Association.class.getCanonicalName() + " (")
				.add(INDENT + "name = \"" + associationSimple.getName() + "\",")
				.add(INDENT + "fkFieldName = \"" + associationSimple.getFKField().getName() + "\",")
				.add(INDENT + "primaryDtDefinitionName = \"" + primaryNode.getDtDefinition().getName() + "\",")
				.add(INDENT + "primaryIsNavigable = " + primaryNode.isNavigable() + ',')
				.add(INDENT + "primaryRole = \"" + primaryNode.getRole() + "\",")
				.add(INDENT + "primaryLabel = \"" + primaryNode.getLabel() + "\",")
				.add(INDENT + "primaryMultiplicity = \"" + primaryMultiplicity + "\",")
				.add(INDENT + "foreignDtDefinitionName = \"" + foreignNode.getDtDefinition().getName() + "\",")
				.add(INDENT + "foreignIsNavigable = " + foreignNode.isNavigable() + ',')
				.add(INDENT + "foreignRole = \"" + foreignNode.getRole() + "\",")
				.add(INDENT + "foreignLabel = \"" + foreignNode.getLabel() + "\",")
				.add(INDENT + "foreignMultiplicity = \"" + foreignMultiplipicity + "\"")
				.add(")")
				.build();
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD gérant une association.
	 *
	 * @param associationNN Definition de l'association
	 * @return Liste des lignes de code java à ajouter.
	 */
	List<String> writeNNAssociationAnnotation(final AssociationNNDefinition associationNN) {
		final AssociationNode nodeA = associationNN.getAssociationNodeA();
		final AssociationNode nodeB = associationNN.getAssociationNodeB();

		return new ListBuilder<String>()
				.add("@" + AssociationNN.class.getCanonicalName() + " (")
				.add(INDENT + "name = \"" + associationNN.getName() + "\",")
				.add(INDENT + "tableName = \"" + associationNN.getTableName() + "\",")
				.add(INDENT + "dtDefinitionA = \"" + nodeA.getDtDefinition().getName() + "\",")
				.add(INDENT + "dtDefinitionB = \"" + nodeB.getDtDefinition().getName() + "\",")
				.add(INDENT + "navigabilityA = " + nodeA.isNavigable() + ',')
				.add(INDENT + "navigabilityB = " + nodeB.isNavigable() + ',')
				.add(INDENT + "roleA = \"" + nodeA.getRole() + "\",")
				.add(INDENT + "roleB = \"" + nodeB.getRole() + "\",")
				.add(INDENT + "labelA = \"" + nodeA.getLabel() + "\",")
				.add(INDENT + "labelB = \"" + nodeB.getLabel() + "\"")
				.add(")")
				.build();
	}
}
