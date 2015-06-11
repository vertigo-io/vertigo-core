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
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.model.DtMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
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
	private final List<TemplateAssociation> templateAssociations = new ArrayList<>();

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

		addTemplateAssociationNodes(Home.getDefinitionSpace().getAll(AssociationSimpleDefinition.class));
		addTemplateAssociationNodes(Home.getDefinitionSpace().getAll(AssociationNNDefinition.class));
	}

	/**
	 * Enregistre toutes les templates d'associations où la DtDéfinition est concernée.
	 */
	private void addTemplateAssociationNodes(final Collection<? extends AssociationDefinition> associationDefinitions) {
		for (final AssociationDefinition associationDefinition : associationDefinitions) {
			if (associationDefinition.getAssociationNodeA().getDtDefinition().getName().equals(dtDefinition.getName())) {
				templateAssociations.add(new TemplateAssociation(associationDefinition.getAssociationNodeB()));
			}
			if (associationDefinition.getAssociationNodeB().getDtDefinition().getName().equals(dtDefinition.getName())) {
				templateAssociations.add(new TemplateAssociation(associationDefinition.getAssociationNodeA()));
			}
		}
	}

	/**
	 * @return Si persistent
	 */
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
	 * @return Simple Nom (i.e. sans le package) de la definition du DtObject
	 */
	public String getClassSimpleNameCamelCase() {
		return StringUtil.constToLowerCamelCase(dtDefinition.getLocalName());
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
	 * @return Nom simple de l'nterface associé au Sterotype de l'objet (DtObject, DtMasterData ou KeyConcept)
	 */
	public String getStereotypeInterfaceName() {
		switch (dtDefinition.getStereotype()) {
			case Data:
				return DtObject.class.getSimpleName();
			case MasterData:
				return DtMasterData.class.getSimpleName();
			case KeyConcept:
				return KeyConcept.class.getSimpleName();
			default:
				throw new IllegalArgumentException("Stereotype " + dtDefinition.getStereotype().name() + " non géré");
		}
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
		return templateAssociations;
	}
}
