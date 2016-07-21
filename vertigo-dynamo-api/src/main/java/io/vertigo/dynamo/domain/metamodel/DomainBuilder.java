package io.vertigo.dynamo.domain.metamodel;

import java.util.Collections;
import java.util.List;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This class must be used to build a Domain.
 * @author pchretien
 */
public final class DomainBuilder implements Builder<Domain> {

	private final String myName;
	private final DataType myDataType;

	/** Formatter. */
	private FormatterDefinition myformatterDefinition;

	/** list of constraints */
	private List<ConstraintDefinition> myConstraintDefinitions;

	/** List of property-value tuples */
	private Properties myProperties;

	/**
	 * Constructor.
	 * @param name the name of the domain
	 * @param dataType the dataType lof the domain
	 */
	public DomainBuilder(final String name, final DataType dataType) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dataType);
		//---
		myName = name;
		myDataType = dataType;
	}

	/**
	 * @param formatterDefinition the FormatterDefinition
	 * @return this builder
	 */
	public DomainBuilder withFormatter(final FormatterDefinition formatterDefinition) {
		Assertion.checkNotNull(formatterDefinition);
		//---
		myformatterDefinition = formatterDefinition;
		return this;
	}

	/**
	 * @param constraintDefinitions the list of constraintDefinitions
	 * @return this builder
	 */
	public DomainBuilder withConstraints(final List<ConstraintDefinition> constraintDefinitions) {
		Assertion.checkNotNull(constraintDefinitions);
		//---
		myConstraintDefinitions = constraintDefinitions;
		return this;
	}

	/**
	 * @param properties the properties
	 * @return this builder
	 */
	public DomainBuilder withProperties(final Properties properties) {
		Assertion.checkNotNull(properties);
		//---
		myProperties = properties;
		return this;
	}

	@Override
	public Domain build() {
		return new Domain(
				myName,
				myDataType,
				myformatterDefinition,
				myConstraintDefinitions == null ? Collections.<ConstraintDefinition> emptyList() : myConstraintDefinitions,
				myProperties == null ? new PropertiesBuilder().build() : myProperties);
	}

}
