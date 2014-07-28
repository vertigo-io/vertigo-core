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

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.vega.rest.metamodel.EndPointDefinition.Verb;
import io.vertigo.vega.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;

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

	public EndPointDefinition build() {
		final String usedPath = myPathPrefix != null ? myPathPrefix + myPath : myPath;
		return new EndPointDefinition(//
				//"EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), //
				"EP_" + myVerb + "_" + StringUtil.camelToConstCase(usedPath.replaceAll("[//{}]", "_")), //
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

	public void withPathPrefix(final String pathPrefix) {
		Assertion.checkArgNotEmpty(pathPrefix, "Route pathPrefix must be specified on {0}", myMethod.getName());
		Assertion.checkArgument(pathPrefix.startsWith("/"), "Route pathPrefix must starts with / (on {0})", myMethod.getName());
		//---------------------------------------------------------------------
		myPathPrefix = pathPrefix;
	}

	public void with(final Verb verb, final String path) {
		Assertion.checkState(myVerb == null, "A verb is already specified on {0} ({1})", myMethod.getName(), myVerb);
		Assertion.checkArgNotEmpty(path, "Route path must be specified on {0}", myMethod.getName());
		Assertion.checkArgument(path.startsWith("/"), "Route path must starts with / (on {0})", myMethod.getName());
		//---------------------------------------------------------------------
		myVerb = verb;
		myPath = path;
	}

	public boolean hasVerb() {
		return myVerb != null;
	}

	public void withAccessTokenConsume(final boolean accessTokenConsume) {
		myAccessTokenConsume = accessTokenConsume;
	}

	public void withNeedAuthentication(final boolean needAuthentication) {
		myNeedAuthentication = needAuthentication;
	}

	public void withNeedSession(final boolean needSession) {
		myNeedSession = needSession;
	}

	public void withSessionInvalidate(final boolean sessionInvalidate) {
		mySessionInvalidate = sessionInvalidate;
	}

	public void withExcludedFields(final String... excludedFields) {
		myExcludedFields.addAll(Arrays.asList(excludedFields));
	}

	public void withIncludedFields(final String... includedFields) {
		myIncludedFields.addAll(Arrays.asList(includedFields));
	}

	public void withAccessTokenPublish(final boolean accessTokenPublish) {
		myAccessTokenPublish = accessTokenPublish;
	}

	public void withAccessTokenMandatory(final boolean accessTokenMandatory) {
		myAccessTokenMandatory = accessTokenMandatory;
	}

	public void withServerSideSave(final boolean serverSideSave) {
		myServerSideSave = serverSideSave;
	}

	public void withAutoSortAndPagination(final boolean autoSortAndPagination) {
		myAutoSortAndPagination = autoSortAndPagination;

		//autoSortAndPagination must keep the list serverSide but not the input one, its the full one, so we don't use serverSideSave marker
		//autoSortAndPagination use a Implicit UiListState, this one must be show in API, so we add it to endPointParams
		//autoSortAndPaginationHandler will use it
		if (autoSortAndPagination) {
			withEndPointParam(new EndPointParamBuilder(UiListState.class) //
					.with(RestParamType.Implicit, ImplicitParam.UiListState.name()).build());
		}
	}

	public void withDoc(final String doc) {
		myDoc = doc;
	}

	public void withEndPointParam(final EndPointParam endPointParam) {
		myEndPointParams.add(endPointParam);
	}
}
