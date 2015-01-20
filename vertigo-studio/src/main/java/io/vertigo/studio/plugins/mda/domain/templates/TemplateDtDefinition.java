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

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author pchretien
 */
public final class TemplateDtDefinition {
	private final DtDefinition dtDefinition;
	private final List<TemplateDtField> dtFields = new ArrayList<>();
	private final List<TemplateDtField> dtComputedFields = new ArrayList<>();
	private final List<TemplateAssociation> associations = new ArrayList<>();

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	public TemplateDtDefinition(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.dtDefinition = dtDefinition;

		for (final DtField dtField : dtDefinition.getFields()) {
			if (FieldType.COMPUTED == dtField.getType()) {
				dtComputedFields.add(new TemplateDtField(dtDefinition, dtField));
			} else {
				dtFields.add(new TemplateDtField(dtDefinition, dtField));
			}
		}
		final Collection<AssociationNode> associationNodeCollection = getTargetAssociationNodes();
		for (final AssociationNode associationNode : associationNodeCollection) {
			associations.add(new TemplateAssociation(associationNode));
		}
	}

	/**
	 * Retourne toutes les associations ou la DtDéfinition est concernée.
	 *
	 * @return Collection des associations concernées
	 */
	private Collection<AssociationNode> getTargetAssociationNodes() {
		final Collection<AssociationDefinition> associationDefinitions = Home.getDefinitionSpace().getAll(AssociationDefinition.class);
		final Collection<AssociationNode> result = new ArrayList<>();
		for (final AssociationDefinition associationDefinition : associationDefinitions) {
			if (associationDefinition.getAssociationNodeA().getDtDefinition().getName().equals(dtDefinition.getName())) {
				result.add(associationDefinition.getAssociationNodeB());
			}
			if (associationDefinition.getAssociationNodeB().getDtDefinition().getName().equals(dtDefinition.getName())) {
				result.add(associationDefinition.getAssociationNodeA());
			}

		}
		return result;
	}

	public boolean isPersistent() {
		return dtDefinition.isPersistent();
	}

	/**
	 * @return DT définition
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return dtDefinition.getClassSimpleName();
	}

	/**
	 * Retourne le nom camelCase de la classe.
	 */
	public String getClassSimpleNameCamelCase() {
		return StringUtil.constToCamelCase(dtDefinition.getLocalName(), false);
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return dtDefinition.getPackageName();
	}

	/**
	 * @return Urn de la définition
	 */
	public String getUrn() {
		return dtDefinition.getName();
	}

	/**
	 * @return Liste de champs
	 */
	public List<TemplateDtField> getDtFields() {
		return dtFields;
	}

	/**
	 * @return Liste des champs calculés
	 */
	public List<TemplateDtField> getDtComputedFields() {
		return dtComputedFields;
	}

	/**
	 * @return Liste des associations
	 */
	public List<TemplateAssociation> getAssociations() {
		return associations;
	}
}
