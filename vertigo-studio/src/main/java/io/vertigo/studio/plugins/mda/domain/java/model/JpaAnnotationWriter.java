/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.domain.java.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;

/**
 * Gestion centralisée des annotations sur les objets générés.
 * Cette implémentation complète les annotations standards et rajoute celles propre à JPA.
 * @author  pchretien, npiedeloup
 */
final class JpaAnnotationWriter extends AnnotationWriter {
	private static final String SEQUENCE_PREFIX = "SEQ_";

	/**
	 * Ectiture des annotations sur le getURI.
	 * @return Liste des lignes de code java à ajouter.
	 */
	@Override
	List<String> writeUriAnnotations() {
		return Collections.singletonList("@javax.persistence.Transient");
	}

	/**
	 * Ectiture des annotations sur une DT_DEFINITION.
	 * @param dtDefinition DtDefinition
	 * @return Liste des lignes de code java à ajouter.
	 */
	@Override
	List<String> writeAnnotations(final DtDefinition dtDefinition) {
		final List<String> lines;
		lines = writeJpaAnnotations(dtDefinition);
		lines.addAll(super.writeAnnotations(dtDefinition));
		return lines;
	}

	/**
	 * Ectiture des annotations sur une DT_DEFINITION.
	 * @param dtDefinition DtDefinition
	 * @return Liste des lignes de code java à ajouter.
	 */
	private static List<String> writeJpaAnnotations(final DtDefinition dtDefinition) {
		final List<String> lines = new ArrayList<>();
		if (dtDefinition.getIdField().isPresent()) { //Il faut un Id pour déclarer l'élément comme Entity. Nous faisons le choix de déclarer comme Entity même les Objects non persistant.
			lines.add("@javax.persistence.Entity");
			if (dtDefinition.isPersistent()) {
				lines.add("@javax.persistence.Table (name = \"" + getTableName(dtDefinition) + "\")");
				if (containsDataStreamField(dtDefinition)) {
					lines.add("@org.hibernate.annotations.TypeDefs(value = { @org.hibernate.annotations.TypeDef(name = \"DO_STREAM\", typeClass = io.vertigo.dynamo.plugins.database.connection.hibernate.DataStreamType.class) })");
				}
			}
		}
		return lines;
	}

	private static boolean containsDataStreamField(final DtDefinition dtDefinition) {
		return dtDefinition.getFields()
				.stream()
				.anyMatch(field -> (field.isPersistent() && field.getDomain().getDataType() == DataType.DataStream));
	}

	/**
	 * Ecriture des annotations sur un DT_FIELD.
	 * @param dtField Champ de la DT_DEFINITION
	 * @return Liste des lignes de code java à ajouter.
	 */
	@Override
	List<String> writeAnnotations(final DtField dtField) {
		final List<String> lines;
		lines = writeJpaAnnotations(dtField);
		lines.addAll(super.writeAnnotations(dtField));
		return lines;
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD.
	 * @param field Champ de la DT_DEFINITION
	 * @return Liste des lignes de code java à ajouter.
	 */
	private static List<String> writeJpaAnnotations(final DtField field) {
		final List<String> lines = new ArrayList<>();

		//Générations des annotations JPA / hibernate
		if (field.getType().isId()) {
			lines.add("@javax.persistence.Id");
			//TODO la gestion des sequences est propre à Oracle, H2, PostgreSql : autres bdd, autres stratégies
			if (field.isPersistent()) {
				final String sequence = getSequenceName(field);
				//allocationSize=1 pour Hibernate 5
				lines.add("@javax.persistence.SequenceGenerator(name = \"sequence\", sequenceName = \"" + sequence + "\", allocationSize=1)");
				lines.add("@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = \"sequence\")");
			}
		}
		if (field.isPersistent()) {
			final String fieldName = field.getName();
			lines.add("@javax.persistence.Column(name = \"" + fieldName + "\")");
			if (!field.isPersistent()) {
				lines.add("@javax.persistence.Transient");
			}
			if (field.isPersistent() && field.getDomain().getDataType() == DataType.DataStream) {
				lines.add("@org.hibernate.annotations.Type(type = \"DO_STREAM\")");
			}
		} else if (field.getType() == DtField.FieldType.COMPUTED) {
			lines.add("@javax.persistence.Transient");
		}
		return lines;
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD gérant une association.
	 * @param associationSimple Definition de l'association
	 * @return Liste des lignes de code java à ajouter.
	 */
	@Override
	List<String> writeSimpleAssociationAnnotation(final AssociationSimpleDefinition associationSimple) {
		final List<String> lines;
		lines = writeJpaAnnotations();
		lines.addAll(super.writeSimpleAssociationAnnotation(associationSimple));
		return lines;
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD gérant une association.
	 * @param associationNN Definition de l'association
	 * @return Liste des lignes de code java à ajouter.
	 */
	@Override
	List<String> writeNNAssociationAnnotation(final AssociationNNDefinition associationNN) {
		final List<String> lines;
		lines = writeJpaAnnotations();
		lines.addAll(super.writeNNAssociationAnnotation(associationNN));
		return lines;
	}

	/**
	 * Ectiture des annotations sur un DT_FIELD gérant une association.
	 * @return Liste des lignes de code java à ajouter.
	 */
	private static List<String> writeJpaAnnotations() {
		final List<String> lines = new ArrayList<>();
		lines.add("@javax.persistence.Transient"); //On ne crée pas de grappe d'objet
		return lines;
	}

	/**
	 * Nom de la table en fonction de la définition du DT mappé.
	 *
	 * @param dtDefinition Définition du DT mappé
	 * @return Nom de la table
	 */
	private static String getTableName(final DtDefinition dtDefinition) {
		return dtDefinition.getLocalName();
	}

	/**
	 * Nom de la séquence utilisée lors des inserts
	 * @param dtDefinition Définition du DT mappé
	 * @return String Nom de la sequence
	 */
	private static String getSequenceName(final DtField field) {
		final DtDefinition dtDefinition = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.filter(definition -> definition.getFields().contains(field))
				.findFirst().orElseThrow(NoSuchElementException::new);

		//oracle n'autorise pas de sequence de plus de 30 char.
		String seqName = SEQUENCE_PREFIX + getTableName(dtDefinition);
		if (seqName.length() > 30) {
			seqName = seqName.substring(0, 30);
		}
		return seqName;
	}
}
