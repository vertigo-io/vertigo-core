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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionPrefix;
import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * The DtDefinition class defines the definition of data.
 *
 * @author pchretien
 */
@DefinitionPrefix("DT")
public final class DtDefinition implements Definition {
	/** the dataSpace must match this pattern. */
	public static final Pattern REGEX_DATA_SPACE = Pattern.compile("[a-z][a-zA-Z0-9]{3,60}");

	/** if the definition is a fragment. */
	private final Optional<DefinitionReference<DtDefinition>> fragment;

	/** name of the definition. */
	private final String name;

	/** name of the package. */
	private final String packageName;

	/** List of fields.  */
	private final List<DtField> fields = new ArrayList<>();

	/** Map. (fieldName, DtField). */
	private final Map<String, DtField> mappedFields = new HashMap<>();

	private final DtStereotype stereotype;

	/** If data is persisted. */
	private final boolean persistent;

	/**
	 * Si la classe est dynamic, c'est à dire non représentée par une classe.
	 */
	private final boolean dynamic;

	/** id Field */
	private final Optional<DtField> idField;

	private Optional<DtField> sortField = Optional.empty();
	private Optional<DtField> displayField = Optional.empty();

	private final String dataSpace;

	/**
	 * Constructeur.
	 */
	DtDefinition(
			final String name,
			final Optional<DefinitionReference<DtDefinition>> fragment,
			final String packageName,
			final DtStereotype stereotype,
			final boolean persistent,
			final List<DtField> dtFields,
			final boolean dynamic,
			final String dataSpace) {
		DefinitionUtil.checkName(name, DtDefinition.class);
		Assertion.checkNotNull(fragment);
		Assertion.checkNotNull(stereotype);
		Assertion.checkNotNull(dtFields);
		Assertion.checkArgNotEmpty(dataSpace);
		Assertion.checkState(REGEX_DATA_SPACE.matcher(dataSpace).matches(), "dataSpace {0} must match pattern {1}", dataSpace, REGEX_DATA_SPACE);
		//-----
		this.name = name;
		//
		this.fragment = fragment;
		//
		this.stereotype = stereotype;
		this.persistent = persistent;
		this.packageName = packageName;
		DtField id = null;

		for (final DtField dtField : dtFields) {
			if (DtField.FieldType.ID.equals(dtField.getType())) {
				Assertion.checkState(id == null, "Only one ID Field is allowed : {0}", name);
				id = dtField;
			}
			doRegisterDtField(dtField);

		}
		idField = Optional.ofNullable(id);
		this.dynamic = dynamic;
		this.dataSpace = dataSpace;
		//-----
		Assertion.checkState(!fragment.isPresent() ^ DtStereotype.Fragment == stereotype, "Error on {0} with sterotype {1}, If an object is a fragment then it must have this stereotype", name, stereotype);
		//Persistent => ID
		Assertion.checkState(!persistent ^ idField.isPresent(), "Error on {0}, If an object is persistent then it must have an ID", name);
		Assertion.checkState(DtStereotype.Data == stereotype ^ idField.isPresent(), "Error on {0}, If an object is a 'Data'  then it must not have an ID", name);
		//NOT Persistent => Data
		Assertion.checkState(persistent ^ DtStereotype.Data == stereotype, "Error on {0}, if an object is not persistent then it must be a simple data", name);
	}

	private void registerSort(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(!sortField.isPresent(), "Un seul champ 'sort' est autorisé par objet : {0}", dtField.getName());
		//-----
		sortField = Optional.of(dtField);
	}

	private void registerDisplay(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(!displayField.isPresent(), "Un seul champ 'display' est autorisé par objet : {0}", dtField.getName());
		//-----
		displayField = Optional.of(dtField);
	}

	//TODO A fermer
	void registerDtField(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(!DtField.FieldType.ID.equals(dtField.getType()), "interdit d'ajouter les champs ID ");
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

	public Optional<DtDefinition> getFragment() {
		if (fragment.isPresent()) {
			return Optional.of(fragment.get().get());
		}
		return Optional.empty();
	}

	/**
	 * @return Stereotype du Dt
	 */
	public DtStereotype getStereotype() {
		return stereotype;
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
		return StringUtil.constToUpperCamelCase(getLocalName());
	}

	/**
	 * @return the name of the package
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
	public boolean contains(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		//-----
		return mappedFields.containsKey(fieldName);
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
	public Optional<DtField> getIdField() {
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
	public Optional<DtField> getDisplayField() {
		return displayField;
	}

	/**
	 * @return Champ représentant le tri
	 */
	public Optional<DtField> getSortField() {
		return sortField;
	}

	/**
	 * @return the dataSpace
	 */
	public String getDataSpace() {
		return dataSpace;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
