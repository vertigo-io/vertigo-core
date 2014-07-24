package io.vertigo.rest.rest.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.rest.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.rest.rest.validation.DtObjectValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private RestParamType restParamType = RestParamType.Body; //default;
	private String restParamName;
	private final List<Class<? extends DtObjectValidator>> validatorClasses = new ArrayList<>();
	private Set<String> includedFields;
	private Set<String> excludedFields;
	private boolean needServerSideToken;
	private boolean consumeServerSideToken;

	/**
	 * Constructeur.
	 */
	public EndPointParamBuilder(final Class<?> paramType) {
		Assertion.checkNotNull(paramType);
		//---------------------------------------------------------------------
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

	public void with(RestParamType restParamType, String restParamName) {
		Assertion.checkNotNull(restParamType);
		Assertion.checkArgNotEmpty(restParamName);
		//---------------------------------------------------------------------
		this.restParamType = restParamType;
		this.restParamName = restParamName;
	}

	public void withValidatorClasses(Class<? extends DtObjectValidator> validatorClass) {
		this.validatorClasses.add(validatorClass);
	}

	public void withValidatorClasses(Class<? extends DtObjectValidator>[] validatorClasses) {
		this.validatorClasses.addAll(Arrays.asList(validatorClasses));
	}

	public void withExcludedFields(String[] excludedFields) {
		this.excludedFields = asSet(excludedFields);
	}

	public void withIncludedFields(String[] includedFields) {
		this.includedFields = asSet(includedFields);
	}

	public void withNeedServerSideToken(boolean needServerSideToken) {
		this.needServerSideToken = needServerSideToken;
	}

	public void withConsumeServerSideToken(boolean consumeServerSideToken) {
		this.consumeServerSideToken = consumeServerSideToken;
	}

	private static Set<String> asSet(final String[] fields) {
		if (fields == null) {
			return Collections.emptySet();
		}
		return new LinkedHashSet<>(Arrays.asList(fields));
	}
}
