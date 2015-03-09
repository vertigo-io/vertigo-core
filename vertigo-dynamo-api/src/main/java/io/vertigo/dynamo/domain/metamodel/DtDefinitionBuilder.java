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
package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.MessageKey;
import io.vertigo.lang.MessageText;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder de définition.
 * Tout DT doit avoir un nom en majuscule préfixé par DT_.
 * Pour obtenir la DtDéfinition utiliser la méthode build();
 *
 * Le DtDefinitionsBuilder doit être flushée.
 *
 * @author pchretien
 */
public final class DtDefinitionBuilder implements Builder<DtDefinition> {
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
	private DtStereotype myStereotype = DtStereotype.Data;
	private boolean myPersistent;
	private boolean myDynamic;
	private final List<DtField> myFields = new ArrayList<>();

	/**
	 * Constructeur.
	 * @param name Definition name
	 */
	public DtDefinitionBuilder(final String name) {
		Assertion.checkArgNotEmpty(name);
		//-----
		myName = name;
	}

	/**
	 * @param packageName Definition's package (nullable)
	 * @return this builder
	 */
	public DtDefinitionBuilder withPackageName(final String packageName) {
		//packageName peut être null
		//-----
		myPackageName = packageName;
		return this;
	}

	/**
	 * @param stereotype Definition's stereotype
	 * @return this builder
	 */
	public DtDefinitionBuilder withStereoType(final DtStereotype stereotype) {
		Assertion.checkNotNull(stereotype);
		//-----
		myStereotype = stereotype;
		return this;
	}

	/**
	 * @param persistent Definition's persistence
	 * @return this builder
	 */
	public DtDefinitionBuilder withPersistent(final boolean persistent) {
		myPersistent = persistent;
		return this;
	}

	/**
	 * @param dynamic If this definition is dynamic
	 * @return this builder
	 */
	public DtDefinitionBuilder withDynamic(final boolean dynamic) {
		myDynamic = dynamic;
		return this;
	}

	/**
	 * Ajout d'une FK.
	 * @param fieldName Nom du champ
	 * @param fkDtDefinitionName Definition référencée
	 * @param label Libellé du champ
	 * @param domain Domain fonctionnel
	 * @param notNull Si la FK est obligatoire
	 * @param sort si champ de tri
	 * @param display si champ de display
	 * @return Builder
	 */
	public DtDefinitionBuilder withForeignKey(final String fieldName, final String label, final Domain domain, final boolean notNull, final String fkDtDefinitionName, final boolean sort, final boolean display) {
		//Pour l'instant on ne gère pas les chamsp computed dynamiques
		final boolean persistent = true;
		final DtField dtField = createField(fieldName, DtField.FieldType.FOREIGN_KEY, domain, label, notNull, persistent, fkDtDefinitionName, null, false, sort, display);
		//On suppose que le build est déjà effectué.
		dtDefinition.registerDtField(dtField);
		return this;
	}

	/**
	 * Ajout d'un champs calculé.
	 * @param domain Domaine associé au champ
	 * @param fieldName Nom du champ
	 * @param label Libellé du champ
	 * @param computedExpression Expression du champs calculé
	 * @param sort si champ de tri
	 * @param display si champ de display
	 * @return Builder
	 */
	public DtDefinitionBuilder withComputedField(final String fieldName, final String label, final Domain domain, final ComputedExpression computedExpression, final boolean sort, final boolean display) {
		//Pour l'instant on ne gère pas les chamsp computed dynamiques
		final DtField dtField = createField(fieldName, DtField.FieldType.COMPUTED, domain, label, false, false, null, computedExpression, false, sort, display);
		myFields.add(dtField);
		return this;
	}

	/**
	 * Ajout d'un champ de type DATA.
	 * @param fieldName Nom du champ
	 * @param domain Domaine associé au champ
	 * @param label Libellé du champ
	 * @param notNull Si le champ est obligatoire
	 * @param persistent Si le champ est persisté
	 * @param sort If this field is use for sorting
	 * @param display If this field is use for display
	 * @return Builder
	 */
	public DtDefinitionBuilder withDataField(final String fieldName, final String label, final Domain domain, final boolean notNull, final boolean persistent, final boolean sort, final boolean display) {
		//le champ  est dynamic SSI la définition est dynamique
		final DtField dtField = createField(fieldName, DtField.FieldType.DATA, domain, label, notNull, persistent, null, null, myDynamic, sort, display);
		myFields.add(dtField);
		return this;
	}

	/**
	 * Ajout d'un champ de type ID.
	 * @param fieldName Nom du champ
	 * @param domain Domaine associé au champ
	 * @param label Libellé du champ
	 * @param sort If this field is use for sorting
	 * @param display If this field is use for display
	 * @return Builder
	 */
	public DtDefinitionBuilder withIdField(final String fieldName, final String label, final Domain domain, final boolean sort, final boolean display) {
		//le champ ID est tjrs notNull
		final boolean notNull = true;
		//le champ ID est persistant SSI la définition est persitante.
		final boolean persistent = myPersistent;
		//le champ  est dynamic SSI la définition est dynamique
		final DtField dtField = createField(fieldName, DtField.FieldType.PRIMARY_KEY, domain, label, notNull, persistent, null, null, myDynamic, sort, display);
		myFields.add(dtField);
		return this;
	}

	private DtField createField(final String fieldName, final DtField.FieldType type, final Domain domain, final String strLabel, final boolean notNull, final boolean persistent, final String fkDtDefinitionName, final ComputedExpression computedExpression, final boolean dynamic, final boolean sort, final boolean display) {

		final String shortName = DefinitionUtil.getLocalName(myName, DtDefinition.class);
		//-----
		// Le DtField vérifie ses propres règles et gère ses propres optimisations
		final String id = DtField.PREFIX + shortName + '$' + fieldName;

		Assertion.checkArgNotEmpty(strLabel, "Label doit être non vide");
		//2. Sinon Indication de longueur portée par le champ du DT.
		//-----
		final MessageText label = new MessageText(strLabel, new MessageKeyImpl(id));
		// Champ CODE_COMMUNE >> getCodeCommune()
		//Un champ est persisanty s'il est marqué comme tel et si la définition l'est aussi.
		return new DtField(id, fieldName, type, domain, label, notNull, persistent && myPersistent, fkDtDefinitionName, computedExpression, dynamic, sort, display);
	}

	/** {@inheritDoc} */
	@Override
	public DtDefinition build() {
		Assertion.checkState(dtDefinition == null, "build already done");
		//-----
		dtDefinition = new DtDefinition(myName, myPackageName, myStereotype, myPersistent, myFields, myDynamic);
		return dtDefinition;
	}

}
