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

import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionPrefix;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Définition d'un type de DT.
 *
 * @author pchretien
 */
@DefinitionPrefix("DT")
public final class DtDefinition implements Definition {
	/** Nom de la définition. */
	private final String name;

	/** Nom du package. */
	private final String packageName;

	/** Liste des champs du DTO.  */
	private final List<DtField> fields = new ArrayList<>();

	/** Map  des champs du DTO. (Nom du champ, DtField). */
	private final Map<String, DtField> mappedFields = new HashMap<>();

	/** Si la classe est persistée. */
	private final boolean persistent;

	/**
	 * Si la classe est dynamic, c'est à dire non représentée par une classe.
	 */
	private final boolean dynamic;

	/** Champ identifiant */
	private final Option<DtField> idField;

	private Option<DtField> sortField = Option.none();
	private Option<DtField> displayField = Option.none();

	/**
	 * Constructeur.
	 */
	DtDefinition(final String name, final String packageName, final boolean persistent, final List<DtField> dtFields, final boolean dynamic) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dtFields);
		//-----
		this.name = name;
		this.persistent = persistent;
		this.packageName = packageName;
		DtField id = null;

		for (final DtField dtField : dtFields) {
			if (DtField.FieldType.PRIMARY_KEY.equals(dtField.getType())) {
				Assertion.checkState(id == null, "Un seul champ identifiant est autorisé par objet : {0}", name);
				id = dtField;
			}
			doRegisterDtField(dtField);

		}
		idField = Option.option(id);
		this.dynamic = dynamic;
		//-----
		Assertion.checkState(!persistent || idField.isDefined(), "Si un DT est persistant il doit posséder un ID");
	}

	private void registerSort(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(sortField.isEmpty(), "Un seul champ 'sort' est autorisé par objet : {0}", dtField.getName());
		//-----
		sortField = Option.some(dtField);
	}

	private void registerDisplay(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(displayField.isEmpty(), "Un seul champ 'display' est autorisé par objet : {0}", dtField.getName());
		//-----
		displayField = Option.some(dtField);
	}

	//TODO A fermer
	void registerDtField(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(!DtField.FieldType.PRIMARY_KEY.equals(dtField.getType()), "interdit d'ajouter les champs ID ");
		//-----
		doRegisterDtField(dtField);
	}

	private void doRegisterDtField(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(!mappedFields.containsKey(dtField.getName()), "Field {0} déjà enregistré sur {1}", dtField.getName(), this);
		//-----
		fields.add(dtField);
		mappedFields.put(dtField.getName(), dtField);
		if (dtField.isSort()) {
			registerSort(dtField);
		}
		if (dtField.isDisplay()) {
			registerDisplay(dtField);
		}
	}

	/**
	 * @return Nom canonique (i.e. avec le package) de la classe d'implémentation du DtObject
	 */
	public String getClassCanonicalName() {
		return getPackageName() + '.' + getClassSimpleName();
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return StringUtil.constToCamelCase(getLocalName(), true);
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Retourne le champ correspondant SOUS CONDITION qu'il existe sinon assertion.
	 *
	 * @param fieldName Nom du champ
	 * @return Champ correspondant
	 */
	public DtField getField(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		//-----
		final DtField dtField = mappedFields.get(fieldName);
		//-----
		Assertion.checkNotNull(dtField, "champ :{0} non trouvé pour le DT :{1}. Liste des champs disponibles :{2}", fieldName, this, mappedFields.keySet());
		return dtField;
	}

	/**
	 * Retourne le champ correspondant SOUS CONDITION qu'il existe sinon assertion.
	 *
	 * @param fieldName Nom du champ
	 * @return Champ correspondant
	 */
	public DtField getField(final DtFieldName fieldName) {
		return getField(fieldName.name());
	}

	/**
	 * @param fieldName FieldName
	 * @return if this field exists in this DtDefinition
	 */
	public boolean contains(final DtFieldName fieldName) {
		Assertion.checkNotNull(fieldName);
		//-----
		return mappedFields.containsKey(fieldName.name());
	}

	/**
	 * @return Collection des champs.
	 */
	public List<DtField> getFields() {
		return Collections.unmodifiableList(fields);
	}

	/**
	 * @return Champ identifiant l'identifiant
	 */
	public Option<DtField> getIdField() {
		return idField;
	}

	/**
	 * @return Si la définition est dynamique. - C'est à dire non représentée par une classe spécifique -
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * Gestion de la persistance.
	 * @return Si la définition est persistée.
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * @return Nom de la définition sans prefix (XXX_YYYY).
	 */
	public String getLocalName() {
		return DefinitionUtil.getLocalName(name, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Champ représentant l'affichage
	 */
	public Option<DtField> getDisplayField() {
		return displayField;
	}

	/**
	 * @return Champ représentant le tri
	 */
	public Option<DtField> getSortField() {
		return sortField;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
