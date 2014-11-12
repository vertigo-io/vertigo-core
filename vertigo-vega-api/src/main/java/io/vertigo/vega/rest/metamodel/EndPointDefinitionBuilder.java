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
import io.vertigo.vega.rest.metamodel.EndPointDefinition.Verb;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.vega.rest.model.UiListState;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * EndPointDefinition Builder.
 *
 * @author npiedeloup
 */
public final class EndPointDefinitionBuilder implements Builder<EndPointDefinition> {
	private final Method myMethod;
	private Verb myVerb;
	private String myPathPrefix;
	private String myPath;
	private final String myAcceptType = "application/json"; //default
	private boolean myNeedSession = true;
	private boolean mySessionInvalidate;
	private boolean myNeedAuthentication = true;
	private final Set<String> myIncludedFields = new LinkedHashSet<>();
	private final Set<String> myExcludedFields = new LinkedHashSet<>();
	private boolean myAccessTokenPublish;
	private boolean myAccessTokenMandatory;
	private boolean myAccessTokenConsume;
	private boolean myServerSideSave;
	private boolean myAutoSortAndPagination;
	private String myDoc = "";
	private final List<EndPointParam> myEndPointParams = new ArrayList<>();

	/**
	 * Constructeur.
	 */
	public EndPointDefinitionBuilder(final Method method) {
		Assertion.checkNotNull(method);
		//---------------------------------------------------------------------
		myMethod = method;
	}

	@Override
	public EndPointDefinition build() {
		final String usedPath = myPathPrefix != null ? myPathPrefix + myPath : myPath;
		final String normalizedPath = usedPath.replaceAll("\\{.*?\\}", "_").replaceAll("[//]", "_"); //.*? : reluctant quantifier

		return new EndPointDefinition(//
				//"EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), //
				"EP_" + myVerb + "_" + normalizedPath.toUpperCase(), //
				myVerb, //
				usedPath, //
				myAcceptType, //
				myMethod, //
				myNeedSession, //
				mySessionInvalidate, //
				myNeedAuthentication, //
				myAccessTokenPublish,//
				myAccessTokenMandatory,//
				myAccessTokenConsume,//
				myServerSideSave,//
				myAutoSortAndPagination,//
				myIncludedFields, //
				myExcludedFields, //
				myEndPointParams, //
				myDoc);
	}

	public EndPointDefinitionBuilder withPathPrefix(final String pathPrefix) {
		Assertion.checkArgNotEmpty(pathPrefix, "Route pathPrefix must be specified on {0}", myMethod.getName());
		Assertion.checkArgument(pathPrefix.startsWith("/"), "Route pathPrefix must starts with / (on {0})", myMethod.getName());
		//---------------------------------------------------------------------
		myPathPrefix = pathPrefix;
		return this;
	}

	public EndPointDefinitionBuilder with(final Verb verb, final String path) {
		Assertion.checkState(myVerb == null, "A verb is already specified on {0} ({1})", myMethod.getName(), myVerb);
		Assertion.checkArgNotEmpty(path, "Route path must be specified on {0}", myMethod.getName());
		Assertion.checkArgument(path.startsWith("/"), "Route path must starts with / (on {0})", myMethod.getName());
		//---------------------------------------------------------------------
		myVerb = verb;
		myPath = path;
		return this;
	}

	public boolean hasVerb() {
		return myVerb != null;
	}

	public EndPointDefinitionBuilder withAccessTokenConsume(final boolean accessTokenConsume) {
		myAccessTokenConsume = accessTokenConsume;
		return this;
	}

	public EndPointDefinitionBuilder withNeedAuthentication(final boolean needAuthentication) {
		myNeedAuthentication = needAuthentication;
		return this;
	}

	public EndPointDefinitionBuilder withNeedSession(final boolean needSession) {
		myNeedSession = needSession;
		return this;
	}

	public EndPointDefinitionBuilder withSessionInvalidate(final boolean sessionInvalidate) {
		mySessionInvalidate = sessionInvalidate;
		return this;
	}

	public EndPointDefinitionBuilder withExcludedFields(final String... excludedFields) {
		myExcludedFields.addAll(Arrays.asList(excludedFields));
		return this;
	}

	public EndPointDefinitionBuilder withIncludedFields(final String... includedFields) {
		myIncludedFields.addAll(Arrays.asList(includedFields));
		return this;
	}

	public EndPointDefinitionBuilder withAccessTokenPublish(final boolean accessTokenPublish) {
		myAccessTokenPublish = accessTokenPublish;
		return this;
	}

	public void withAccessTokenMandatory(final boolean accessTokenMandatory) {
		myAccessTokenMandatory = accessTokenMandatory;
	}

	public EndPointDefinitionBuilder withServerSideSave(final boolean serverSideSave) {
		myServerSideSave = serverSideSave;
		return this;
	}

	public EndPointDefinitionBuilder withAutoSortAndPagination(final boolean autoSortAndPagination) {
		myAutoSortAndPagination = autoSortAndPagination;

		//autoSortAndPagination must keep the list serverSide but not the input one, its the full one, so we don't use serverSideSave marker
		//autoSortAndPagination use a Implicit UiListState, this one must be show in API, so we add it to endPointParams
		//autoSortAndPaginationHandler will use it
		if (autoSortAndPagination) {
			withEndPointParam(new EndPointParamBuilder(UiListState.class)
					.with(RestParamType.Body, "listState") // We declare ListState in body, it will merge with other EndPointParams
					.build());
		}
		return this;
	}

	public EndPointDefinitionBuilder withDoc(final String doc) {
		myDoc = doc;
		return this;
	}

	public EndPointDefinitionBuilder withEndPointParam(final EndPointParam endPointParam) {
		myEndPointParams.add(endPointParam);
		return this;
	}
}
