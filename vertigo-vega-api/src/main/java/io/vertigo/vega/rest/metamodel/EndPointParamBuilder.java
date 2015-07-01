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
package io.vertigo.vega.rest.metamodel;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.vega.rest.validation.DtObjectValidator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EndPointParam Builder.
 *
 * @author npiedeloup
 */
public final class EndPointParamBuilder implements Builder<EndPointParam> {
	private final Type myParamType;
	private RestParamType myRestParamType = RestParamType.Body; // default;
	private String myRestParamName = "[1]"; //default body Name
	private final List<Class<? extends DtObjectValidator>> myValidatorClasses = new ArrayList<>();
	private final Set<String> myIncludedFields = new HashSet<>();
	private final Set<String> myExcludedFields = new HashSet<>();
	private boolean myNeedServerSideToken;
	private boolean myConsumeServerSideToken;

	/**
	 * Constructeur.
	 * @param paramType param type
	 */
	public EndPointParamBuilder(final Type paramType) {
		Assertion.checkNotNull(paramType);
		//-----
		myParamType = paramType;
	}

	/**
	 * @param restParamType paramType
	 * @param restParamName paramName
	 * @return Builder
	 */
	public EndPointParamBuilder with(final RestParamType restParamType, final String restParamName) {
		Assertion.checkNotNull(restParamType);
		Assertion.checkNotNull(restParamName); //empty names were check on EndPointParam constructor
		//-----
		myRestParamType = restParamType;
		myRestParamName = restParamName;
		return this;
	}

	/**
	 * @param validatorClasses List of validator to check
	 * @return Builder
	 */
	public EndPointParamBuilder addValidatorClasses(final Class<? extends DtObjectValidator>... validatorClasses) {
		Assertion.checkNotNull(validatorClasses);
		//-----
		myValidatorClasses.addAll(Arrays.asList(validatorClasses));
		return this;
	}

	/**
	 * @param excludedFields List of exluded fields
	 * @return Builder
	 */
	public EndPointParamBuilder addExcludedFields(final String... excludedFields) {
		Assertion.checkNotNull(excludedFields);
		//-----
		myExcludedFields.addAll(Arrays.asList(excludedFields));
		return this;
	}

	/**
	 * @param includedFields list of included fields (empty means all fields included)
	 * @return Builder
	 */
	public EndPointParamBuilder addIncludedFields(final String... includedFields) {
		Assertion.checkNotNull(includedFields);
		//-----
		myIncludedFields.addAll(Arrays.asList(includedFields));
		return this;
	}

	/**
	 * If serverSide token is needed and used
	 * @return Builder
	 */
	public EndPointParamBuilder needServerSideToken() {
		myNeedServerSideToken = true;
		return this;
	}

	/**
	 * If serverSide token is consume
	 * @return Builder
	 */
	public EndPointParamBuilder consumeServerSideToken() {
		myConsumeServerSideToken = true;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public EndPointParam build() {
		return new EndPointParam(
				myRestParamType,
				myRestParamName,
				myParamType,
				myIncludedFields,
				myExcludedFields,
				myNeedServerSideToken,
				myConsumeServerSideToken,
				myValidatorClasses);
	}
}
