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
package io.vertigo.dynamo.domain.metamodel;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.app.Home;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * A domain exists to enrich the primitive datatypes, giving them super powers.
 *
 * A domain has
 *  - a validator (executed by a list of constraints)
 *  - a formatter
 *
 * A domain is a shared object ; by nature it is immutable.
 *
 * A domain is a definition, its prefix is "DO_"
 *
 * Examples :
 *  A mail is not defined by a simple "String", but by a domain called 'Mail'.
 *  Weights, currencies, codes, labels...
 *
 *  An application is built with some dozens of domains.
 *
 * @author pchretien
 */
@DefinitionPrefix("DO")
public final class Domain implements Definition {
	private final String name;
	private final boolean multiple;
	private final DataType dataType;
	/**
	 * Name of the DtDefinition (for the DtObject or DtList)
	 */
	private final String dtDefinitionName;

	/** Formatter. */
	private final DefinitionReference<FormatterDefinition> formatterDefinitionRef;

	/** Validator composed by a list of constraints. */
	private final List<DefinitionReference<ConstraintDefinition>> constraintDefinitionRefs;

	/** List of property-value tuples */
	private final Properties properties;

	/**
	 * Constructor.
	 * @param name the name of the domain
	 * @param dataType the type of the domain
	 * @param formatterDefinition the formatter
	 * @param constraintDefinitions the list of constraints
	 * @param properties List of property-value tuples
	 */
	Domain(
			final String name,
			final DataType dataType,
			final String dtDefinitionName,
			final boolean multiple,
			final FormatterDefinition formatterDefinition,
			final List<ConstraintDefinition> constraintDefinitions,
			final Properties properties) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkState(dataType != null ^ dtDefinitionName != null, "a domain must have a primitive xor a dtDefinition");
		//formatterDefinition can be null
		Assertion.checkNotNull(constraintDefinitions);
		Assertion.checkNotNull(properties);
		//-----
		this.name = name;
		this.dataType = dataType;
		this.dtDefinitionName = dtDefinitionName;
		this.multiple = multiple;
		formatterDefinitionRef = formatterDefinition == null ? null : new DefinitionReference<>(formatterDefinition);
		//---Constraints
		constraintDefinitionRefs = buildConstraintDefinitionRefs(constraintDefinitions);
		//---Properties
		this.properties = buildProperties(constraintDefinitions, properties);
	}

	/**
	 * Static method factory for DomainBuilder
	 * @param name the name of the domain
	 * @param dataType the dataType lof the domain
	 * @return DomainBuilder
	 */
	public static DomainBuilder builder(final String name, final DataType dataType, final boolean multiple) {
		return new DomainBuilder(name, dataType, multiple);
	}

	public static DomainBuilder builder(final String name, final DataType dataType) {
		return new DomainBuilder(name, dataType, false);
	}

	public static DomainBuilder builder(final String name, final String dtDefinitionName, final boolean multiple) {
		return new DomainBuilder(name, dtDefinitionName, multiple);
	}

	private static List<DefinitionReference<ConstraintDefinition>> buildConstraintDefinitionRefs(final List<ConstraintDefinition> constraintDefinitions) {
		return constraintDefinitions
				.stream()
				.map(DefinitionReference::new)
				.collect(Collectors.toList());
	}

	private static Properties buildProperties(final List<ConstraintDefinition> constraintDefinitions, final Properties inputProperties) {
		final PropertiesBuilder propertiesBuilder = Properties.builder();
		for (final Property property : inputProperties.getProperties()) {
			propertiesBuilder.addValue(property, inputProperties.getValue(property));
		}

		//Properties are inferred from constraints
		for (final ConstraintDefinition constraintDefinition : constraintDefinitions) {
			propertiesBuilder.addValue(constraintDefinition.getProperty(), constraintDefinition.getPropertyValue());
		}
		return propertiesBuilder.build();
	}

	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * Returns the dataType of the domain.
	 *
	 * @return the dataType.
	 */
	public DataType getDataType() {
		Assertion.checkNotNull(dataType, "can only be used with primitives");
		//---
		return dataType;
	}

	/**
	 * Returns the formatter of the domain.
	 *
	 * @return the formatter.
	 */
	private FormatterDefinition getFormatter() {
		Assertion.checkNotNull(formatterDefinitionRef, "no formatter defined on {0}", this);
		//-----
		return formatterDefinitionRef.get();
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Chechs if the value is valid.
	 *
	 * @param value the value to check
	 */
	public void checkValue(final Object value) {
		if (isPrimitive()) {
			if (isMultiple()) {
				if (!(value instanceof List)) {
					throw new ClassCastException("Value " + value + " must be a list");
				}
				List.class.cast(value).forEach(element -> dataType.checkValue(element));
			} else {
				dataType.checkValue(value);
			}
		}
	}

	/**
	 * Chechs if
	 *  - the value is valid
	 *  - the constraints are ok.
	 *
	 * @param value the value to check
	 * @throws ConstraintException if a constraint has failed
	 */
	public void checkConstraints(final Object value) throws ConstraintException {
		checkValue(value);
		//---
		for (final DefinitionReference<ConstraintDefinition> constraintDefinitionRef : constraintDefinitionRefs) {
			//when a constraint fails, there is no validation
			if (!constraintDefinitionRef.get().checkConstraint(value)) {
				throw new ConstraintException(constraintDefinitionRef.get().getErrorMessage());
			}
		}
	}

	//==========================================================================
	//for these domains : DtList or DtObject
	//==========================================================================
	/**
	 * @return the dtDefinition for the domains DtList or DtObject.
	 */
	public DtDefinition getDtDefinition() {
		Assertion.checkState(isDtObject() || isDtList(), "only DtObject or DtList can have a DtDefinition");
		//---
		return Home.getApp().getDefinitionSpace().resolve(dtDefinitionName, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	public boolean isPrimitive() {
		return dataType != null;
	}

	public boolean isDtList() {
		return dtDefinitionName != null && multiple;
	}

	public boolean isDtObject() {
		return dtDefinitionName != null && !multiple;
	}

	public Class getJavaClass() {
		if (isPrimitive()) {
			return dataType.getJavaClass();
		}
		return ClassUtil.classForName(getDtDefinition().getClassCanonicalName());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

	public String getFormatterClassName() {
		Assertion.checkNotNull(dataType, "can only be used with primitives");
		//---
		return getFormatter().getFormatterClassName();
	}

	public String valueToString(final Object objValue) {
		Assertion.checkNotNull(dataType, "can only be used with primitives");
		//---
		return getFormatter().valueToString(objValue, dataType);
	}

	public Object stringToValue(final String strValue) throws FormatterException {
		Assertion.checkNotNull(dataType, "can only be used with primitives");
		//---
		return getFormatter().stringToValue(strValue, dataType);
	}
}
