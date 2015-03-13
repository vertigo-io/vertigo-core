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

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.util.StringUtil;
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
	 * @param method Method to bind to this endpoint
	 */
	public EndPointDefinitionBuilder(final Method method) {
		Assertion.checkNotNull(method);
		//-----
		myMethod = method;
	}

	/** {@inheritDoc} */
	@Override
	public EndPointDefinition build() {
		final String usedPath = myPathPrefix != null ? myPathPrefix + myPath : myPath;
		final String normalizedPath = normalizePath(usedPath);
		final String acceptedType = computeAcceptedType();
		return new EndPointDefinition(
				//"EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()),
				"EP_" + myVerb + "_" + normalizedPath.toUpperCase(),
				myVerb,
				usedPath,
				acceptedType,
				myMethod,
				myNeedSession,
				mySessionInvalidate,
				myNeedAuthentication,
				myAccessTokenPublish,
				myAccessTokenMandatory,
				myAccessTokenConsume,
				myServerSideSave,
				myAutoSortAndPagination,
				myIncludedFields,
				myExcludedFields,
				myEndPointParams,
				myDoc);
	}

	private String normalizePath(final String servicePath) {
		return servicePath.replaceAll("\\{.*?\\}", "_")//.*? : reluctant quantifier;
				.replaceAll("[//\\*]", "_");
	}

	/**
	 * @param pathPrefix Path prefix
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withPathPrefix(final String pathPrefix) {
		Assertion.checkArgNotEmpty(pathPrefix, "Route pathPrefix must be specified on {0}.{1}", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName());
		Assertion.checkArgument(pathPrefix.startsWith("/"), "Route pathPrefix must starts with / (on {0}.{1})", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName());
		//-----
		myPathPrefix = pathPrefix;
		return this;
	}

	/**
	 * @param verb Verb
	 * @param path Path
	 * @return this builder
	 */
	public EndPointDefinitionBuilder with(final Verb verb, final String path) {
		Assertion.checkState(myVerb == null, "A verb is already specified on {0}.{1} ({2})", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName(), myVerb);
		Assertion.checkArgument(!StringUtil.isEmpty(myPathPrefix) || !StringUtil.isEmpty(path), "Route path must be specified on {0}.{1} (at least you should defined a pathPrefix)", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName());
		//-----
		myVerb = verb;
		myPath = path;
		return this;
	}

	/**
	 * @return if verb was set
	 */
	public boolean hasVerb() {
		return myVerb != null;
	}

	/**
	 * @param accessTokenConsume accessTokenConsume
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withAccessTokenConsume(final boolean accessTokenConsume) {
		myAccessTokenConsume = accessTokenConsume;
		return this;
	}

	/**
	 * @param needAuthentication needAuthentication
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withNeedAuthentication(final boolean needAuthentication) {
		myNeedAuthentication = needAuthentication;
		return this;
	}

	/**
	 * @param needSession needSession
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withNeedSession(final boolean needSession) {
		myNeedSession = needSession;
		return this;
	}

	/**
	 * @param sessionInvalidate sessionInvalidate
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withSessionInvalidate(final boolean sessionInvalidate) {
		mySessionInvalidate = sessionInvalidate;
		return this;
	}

	/**
	 * @param excludedFields list of excludedFields
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withExcludedFields(final String... excludedFields) {
		myExcludedFields.addAll(Arrays.asList(excludedFields));
		return this;
	}

	/**
	 * @param includedFields list of includedFields
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withIncludedFields(final String... includedFields) {
		myIncludedFields.addAll(Arrays.asList(includedFields));
		return this;
	}

	/**
	 * @param accessTokenPublish accessTokenPublish
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withAccessTokenPublish(final boolean accessTokenPublish) {
		myAccessTokenPublish = accessTokenPublish;
		return this;
	}

	/**
	 * @param accessTokenMandatory accessTokenMandatory
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withAccessTokenMandatory(final boolean accessTokenMandatory) {
		myAccessTokenMandatory = accessTokenMandatory;
		return this;
	}

	/**
	 * @param serverSideSave serverSideSave
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withServerSideSave(final boolean serverSideSave) {
		myServerSideSave = serverSideSave;
		return this;
	}

	/**
	 * @param autoSortAndPagination autoSortAndPagination
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withAutoSortAndPagination(final boolean autoSortAndPagination) {
		myAutoSortAndPagination = autoSortAndPagination;

		//autoSortAndPagination must keep the list serverSide but not the input one, its the full one, so we don't use serverSideSave marker
		//autoSortAndPagination use a Implicit UiListState, this one must be show in API, so we add it to endPointParams
		//autoSortAndPaginationHandler will use it
		if (autoSortAndPagination) {
			withEndPointParam(new EndPointParamBuilder(UiListState.class)
					.with(RestParamType.Query, "") // We declare ListState in query without prefix
					.build());
		}
		return this;
	}

	/**
	 * @param doc doc
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withDoc(final String doc) {
		myDoc = doc;
		return this;
	}

	/**
	 * @param endPointParam endPointParam
	 * @return this builder
	 */
	public EndPointDefinitionBuilder withEndPointParam(final EndPointParam endPointParam) {
		myEndPointParams.add(endPointParam);
		return this;
	}

	private String computeAcceptedType() {
		for (final EndPointParam endPointParam : myEndPointParams) {
			if (VFile.class.isAssignableFrom(endPointParam.getType())) {
				return "multipart/form-data";
			} else if (endPointParam.getParamType() == RestParamType.Query) {
				if (myVerb != Verb.GET) {//if GET => nothing
					return "application/x-www-form-urlencoded";
				}
			} else if (endPointParam.getParamType() == RestParamType.Body || endPointParam.getParamType() == RestParamType.InnerBody) {
				return "application/json";
			}
		}
		return "application/json";
	}

}
