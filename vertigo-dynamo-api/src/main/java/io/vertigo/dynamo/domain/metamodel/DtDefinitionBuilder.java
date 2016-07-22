/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.domain.metamodel;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.MessageKey;
import io.vertigo.lang.MessageText;

/**
 * This class must be used to build a DtDefinition.
 *
 * Each dtDefinition must have a name following this pattern DT_XXX_YYYY
 *
 * @author pchretien
 */
public final class DtDefinitionBuilder implements Builder<DtDefinition> {
	public static final String DEFAULT_DATA_SPACE = "main";

	private static class MessageKeyImpl implements MessageKey {
		private static final long serialVersionUID = 6959551752755175151L;

		private final String name;

		MessageKeyImpl(final String name) {
			this.name = name;
		}

		/** {@inheritDoc} */
		@Override
		public String name() {
			return name;
		}
	}

	private DtDefinition dtDefinition;
	private final String myName;
	private String myPackageName;
	private DtStereotype myStereotype;
	private boolean myPersistent;
	private boolean myDynamic;
	private DtField myIdField;
	private final List<DtField> myFields = new ArrayList<>();
	private String myDataSpace;

	/**
	 * Constructor.
	 * @param name the name of the dtDefinition
	 */
	public DtDefinitionBuilder(final String name) {
		Assertion.checkArgNotEmpty(name);
		//-----
		myName = name;
	}

	/**
	 * Sets packageName
	 * @param packageName the name of the package (nullable)
	 * @return this builder
	 */
	public DtDefinitionBuilder withPackageName(final String packageName) {
		//the packageName can be null
		//-----
		myPackageName = packageName;
		return this;
	}

	/**
	 * Sets the stereotype of the dtDefinition.
	 *
	 * @param stereotype the stereotype of the dtDefinition
	 * @return this builder
	 */
	public DtDefinitionBuilder withStereoType(final DtStereotype stereotype) {
		Assertion.checkNotNull(stereotype);
		//-----
		myStereotype = stereotype;
		return this;
	}

	/**
	 * Sets the persistent state.
	 *
	 * @param persistent if the dtDefinition is persisted
	 * @return this builder
	 */
	public DtDefinitionBuilder withPersistent(final boolean persistent) {
		myPersistent = persistent;
		return this;
	}

	/**
	 * Sets the dynamic state.
	 *
	 * @param dynamic if this dtDefinition is dynamic
	 * @return this builder
	 */
	public DtDefinitionBuilder withDynamic(final boolean dynamic) {
		myDynamic = dynamic;
		return this;
	}

	/**
	 * Adds a field linked to another dtDefinition (aka foreign key).
	 *
	 * @param fieldName the name of the field
	 * @param fkDtDefinitionName the name of the linked definition
	 * @param label the label of the field
	 * @param domain the domain of the field
	 * @param required if the field is required
	 * @param sort if this field is use for sorting
	 * @param display if this field is use for display
	 * @return this builder
	 */
	public DtDefinitionBuilder addForeignKey(final String fieldName, final String label, final Domain domain, final boolean required, final String fkDtDefinitionName, final boolean sort, final boolean display) {
		//Pour l'instant on ne gère pas les chamsp computed dynamiques
		final boolean persistent = true;
		final DtField dtField = createField(fieldName, DtField.FieldType.FOREIGN_KEY, domain, label, required, persistent, fkDtDefinitionName, null, false, sort, display);
		//On suppose que le build est déjà effectué.
		dtDefinition.registerDtField(dtField);
		return this;
	}

	/**
	 * Adds a computed field.
	 *
	 * @param fieldName the name of the field
	 * @param label the label of the field
	 * @param domain the domain of the field
	 * @param computedExpression the expression use to compute the field
	 * @param sort if this field is use for sorting
	 * @param display if this field is use for display
	 * @return this builder
	 */
	public DtDefinitionBuilder addComputedField(final String fieldName, final String label, final Domain domain, final ComputedExpression computedExpression, final boolean sort, final boolean display) {
		//Pour l'instant on ne gère pas les chamsp computed dynamiques
		final DtField dtField = createField(fieldName, DtField.FieldType.COMPUTED, domain, label, false, false, null, computedExpression, false, sort, display);
		myFields.add(dtField);
		return this;
	}

	/**
	 * Adds a common data field.
	 *
	 * @param fieldName the name of the field
	 * @param domain the domain of the field
	 * @param label the label of the field
	 * @param required if the field is required
	 * @param persistent if the fiels is persistent
	 * @param sort if this field is use for sorting
	 * @param display if this field is use for display
	 * @return this builder
	 */
	public DtDefinitionBuilder addDataField(final String fieldName, final String label, final Domain domain, final boolean required, final boolean persistent, final boolean sort, final boolean display) {
		//the field is dynamic if and only if the dtDefinition is dynamic
		final DtField dtField = createField(fieldName, DtField.FieldType.DATA, domain, label, required, persistent, null, null, myDynamic, sort, display);
		myFields.add(dtField);
		return this;
	}

	/**
	 * Adds an ID field.
	 * This field is required.
	 *
	 * @param fieldName the name of the field
	 * @param domain the domain of the field
	 * @param label the label of the field
	 * @param sort if this field is use for sorting
	 * @param display if this field is use for display
	 * @return this builder
	 */
	public DtDefinitionBuilder addIdField(final String fieldName, final String label, final Domain domain, final boolean sort, final boolean display) {
		Assertion.checkArgument(myIdField == null, "only one ID per Entity is permitted, error on {0}", myPackageName);
		//---
		//le champ ID est tjrs required
		final boolean required = true;
		//le champ ID est persistant SSI la définition est persitante.
		final boolean persistent = myPersistent;
		//le champ  est dynamic SSI la définition est dynamique
		final DtField dtField = createField(fieldName, DtField.FieldType.ID, domain, label, required, persistent, null, null, myDynamic, sort, display);
		myIdField = dtField;
		myFields.add(dtField);
		return this;
	}

	private DtField createField(final String fieldName, final DtField.FieldType type, final Domain domain, final String strLabel, final boolean required, final boolean persistent, final String fkDtDefinitionName, final ComputedExpression computedExpression, final boolean dynamic, final boolean sort, final boolean display) {

		final String shortName = DefinitionUtil.getLocalName(myName, DtDefinition.class);
		//-----
		// Le DtField vérifie ses propres règles et gère ses propres optimisations
		final String id = DtField.PREFIX + shortName + '$' + fieldName;

		Assertion.checkArgNotEmpty(strLabel, "Label must not be empty");
		//2. Sinon Indication de longueur portée par le champ du DT.
		//-----
		final MessageText label = new MessageText(strLabel, new MessageKeyImpl(id));
		// Champ CODE_COMMUNE >> getCodeCommune()
		//Un champ est persisanty s'il est marqué comme tel et si la définition l'est aussi.
		return new DtField(id, fieldName, type, domain, label, required, persistent && myPersistent, fkDtDefinitionName, computedExpression, dynamic, sort, display);
	}

	/**
	 * Sets the dataSpace to which the dtDefinition belongs.
	 * @param dataSpace the dataSpace to which the DtDefinition is mapped.
	 * @return this builder
	 */
	public DtDefinitionBuilder withDataSpace(final String dataSpace) {
		//the dataSpace can be null, in this case the default dataSpace will be chosen.
		//-----
		myDataSpace = dataSpace;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DtDefinition build() {
		Assertion.checkState(dtDefinition == null, "build() already executed");
		//-----
		if (myStereotype == null) {
			myStereotype = (myIdField == null) ? DtStereotype.Data : DtStereotype.Entity;
		}
		dtDefinition = new DtDefinition(myName, myPackageName, myStereotype, myPersistent, myFields, myDynamic, myDataSpace == null ? DEFAULT_DATA_SPACE : myDataSpace);
		return dtDefinition;
	}
}
