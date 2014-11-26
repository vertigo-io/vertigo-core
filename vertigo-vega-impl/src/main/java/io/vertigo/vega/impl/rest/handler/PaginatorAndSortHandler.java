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
package io.vertigo.vega.impl.rest.handler;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.model.UiListState;
import io.vertigo.vega.token.TokenManager;
import spark.Request;
import spark.Response;

/**
 * Auto paginator and Sort handler.
 * @author npiedeloup
 */
final class PaginatorAndSortHandler implements RouteHandler {
	private static final int DEFAULT_RESULT_PER_PAGE = 20;

	private final TokenManager uiTokenManager;
	private final CollectionsManager collectionsManager;
	private final EndPointDefinition endPointDefinition;

	/**
	 * Constructor.
	 * @param endPointDefinition endpoint definition
	 * @param collectionsManager collections manager
	 * @param uiSecurityTokenManager token manager
	 */
	public PaginatorAndSortHandler(final EndPointDefinition endPointDefinition, final CollectionsManager collectionsManager, final TokenManager uiSecurityTokenManager) {
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(uiSecurityTokenManager);
		//---------------------------------------------------------------------
		this.endPointDefinition = endPointDefinition;
		this.collectionsManager = collectionsManager;
		uiTokenManager = uiSecurityTokenManager;
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		//Criteria in body
		//UiListState in query //see at EndPointDefinitionBuilder withAutoSortAndPagination it defined where UiListState was
		//serverToken in UiListState

		final EndPointParam uiListEndPointParams = lookupEndPointParam(endPointDefinition, UiListState.class);
		Assertion.checkNotNull(uiListEndPointParams, "sort and pagination need a UiListState endpointParams. It should have been added by EndPointParamBuilder.");

		final UiListState parsedUiListState = (UiListState) routeContext.getParamValue(uiListEndPointParams);
		final UiListState uiListState = checkAndEnsureDefaultValue(parsedUiListState);

		String listServerToken = uiListState.getListServerToken();
		Option<DtList<?>> fullListOption = Option.none();
		if (listServerToken != null) {
			fullListOption = uiTokenManager.<DtList<?>> get(uiListState.getListServerToken());
		}
		final DtList<?> fullList;
		if (fullListOption.isDefined()) {
			fullList = fullListOption.get();
		} else {
			final Object result = chain.handle(request, response, routeContext);
			Assertion.checkArgument(result instanceof DtList, "sort and pagination only supports DtList");
			fullList = (DtList<?>) result;
			listServerToken = uiTokenManager.put(fullList);
		}
		response.header("listServerToken", listServerToken);
		response.header("x-total-count", String.valueOf(fullList.size())); //TODO total count should be list meta
		final DtList<?> filteredList = applySortAndPagination(fullList, uiListState);
		filteredList.setMetaData("total-count", fullList.size());
		return filteredList;
	}

	private static UiListState checkAndEnsureDefaultValue(final UiListState parsedUiListState) {
		if (parsedUiListState.getTop() == 0) {//check if parsedUiListState, is just not initalized
			return new UiListState(DEFAULT_RESULT_PER_PAGE, parsedUiListState.getSkip(), null, true, null);
		}
		return parsedUiListState;
	}

	/**
	 * Lookup for a parameter of the asked type, return the first found.
	 * @param endPointDefinition EndPoint definition
	 * @param paramType Type asked
	 * @return first EndPointParam of this type, null if not found
	 */
	private static EndPointParam lookupEndPointParam(final EndPointDefinition endPointDefinition, final Class<UiListState> paramType) {
		for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
			if (paramType.equals(endPointParam.getType())) {
				return endPointParam;
			}
		}
		return null;
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final UiListState uiListState) {
		final DtList<D> sortedList;
		if (uiListState.getSortFieldName() != null) {
			sortedList = collectionsManager.createDtListProcessor()
					.sort(StringUtil.camelToConstCase(uiListState.getSortFieldName()), uiListState.isSortDesc(), true, true)
					.apply(unFilteredList);
		} else {
			sortedList = unFilteredList;
		}
		final DtList<D> filteredList;
		if (uiListState.getSkip() >= sortedList.size()) {
			filteredList = new DtList<>(unFilteredList.getDefinition());
		} else if (uiListState.getTop() > 0) {
			final int start = uiListState.getSkip();
			final int end = Math.min(start + uiListState.getTop(), sortedList.size());
			filteredList = collectionsManager.createDtListProcessor()
					.filterSubList(start, end)
					.apply(sortedList);
		} else {
			filteredList = sortedList;
		}
		return filteredList;
	}
}
