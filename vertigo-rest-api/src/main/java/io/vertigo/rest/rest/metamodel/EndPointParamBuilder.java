package io.vertigo.rest.rest.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.rest.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.rest.rest.validation.DtObjectValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * EndPointParam Builder.
 * 
 * @author npiedeloup
 */
public final class EndPointParamBuilder implements Builder<EndPointParam> {
	private final Class<?> paramType;
	private RestParamType restParamType = RestParamType.Body; // default;
	private String restParamName;
	private final List<Class<? extends DtObjectValidator>> validatorClasses = new ArrayList<>();
	private Set<String> includedFields = new LinkedHashSet<>();
	private Set<String> excludedFields = new LinkedHashSet<>();
	private boolean needServerSideToken;
	private boolean consumeServerSideToken;

	/**
	 * Constructeur.
	 */
	public EndPointParamBuilder(final Class<?> paramType) {
		Assertion.checkNotNull(paramType);
		// ---------------------------------------------------------------------
		this.paramType = paramType;
	}

	public EndPointParam build() {
		if (restParamType == RestParamType.Body) {
			return new EndPointParam(restParamType, paramType, //
					includedFields, //
					excludedFields, //
					needServerSideToken, //
					consumeServerSideToken, //
					validatorClasses);
		}
		return new EndPointParam(restParamType, restParamName, paramType, //
				includedFields, //
				excludedFields, //
				needServerSideToken, //
				consumeServerSideToken, //
				validatorClasses);
	}

	/**
	 * @param restParamType
	 * @param restParamName
	 * @return this builder
	 */
	public EndPointParamBuilder with(RestParamType restParamType,String restParamName) {
		Assertion.checkNotNull(restParamType);
		Assertion.checkArgNotEmpty(restParamName);
		// ---------------------------------------------------------------------
		this.restParamType = restParamType;
		this.restParamName = restParamName;
		return this;
	}


	/**
	 * @param validatorClasses List of validator to check
	 * @return this builder
	 */
	public EndPointParamBuilder withValidatorClasses(
			Class<? extends DtObjectValidator>... validatorClasses) {
		this.validatorClasses.addAll(Arrays.asList(validatorClasses));
		return this;
	}

	/**
	 * @param excludedFields List of exluded fields
	 * @return this builder
	 */
	public EndPointParamBuilder withExcludedFields(String[] excludedFields) {
		this.excludedFields.addAll(Arrays.asList(excludedFields));
		return this;
	}

	/**
	 * @param includedFields list of included fields (empty means all fields included)
	 * @return
	 */
	public EndPointParamBuilder withIncludedFields(String[] includedFields) {
		this.includedFields.addAll(Arrays.asList(includedFields));
		return this;
	}

	/**
	 * @param needServerSideToken is serverSide token is needed and used
	 * @return
	 */
	public EndPointParamBuilder withNeedServerSideToken(boolean needServerSideToken) {
		this.needServerSideToken = needServerSideToken;
		return this;
	}

	/**
	 * @param consumeServerSideToken if serverSide token is consume
	 * @return this builder
	 */
	public EndPointParamBuilder withConsumeServerSideToken(boolean consumeServerSideToken) {
		this.consumeServerSideToken = consumeServerSideToken;
		return this;
	}
}
