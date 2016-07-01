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
package io.vertigo.vega.plugins.webservice.handler;

import javax.inject.Inject;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.token.TokenManager;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.model.UiListState;
import spark.Request;
import spark.Response;

/**
 * Auto paginator and Sort handler.
 * @author npiedeloup
 */
public final class PaginatorAndSortWebServiceHandlerPlugin implements WebServiceHandlerPlugin {
	private static final int DEFAULT_RESULT_PER_PAGE = 20;

	private final TokenManager tokenManager;
	private final CollectionsManager collectionsManager;

	/**
	 * Constructor.
	 * @param collectionsManager collections manager
	 * @param tokenManager token manager
	 */
	@Inject
	public PaginatorAndSortWebServiceHandlerPlugin(final CollectionsManager collectionsManager, final TokenManager tokenManager) {
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(tokenManager);
		//-----
		this.collectionsManager = collectionsManager;
		this.tokenManager = tokenManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return webServiceDefinition.isAutoSortAndPagination();
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {

		final WebServiceDefinition webServiceDefinition = routeContext.getWebServiceDefinition();
		//Criteria in body
		//UiListState in query //see at WebServiceDefinitionBuilder withAutoSortAndPagination it defined where UiListState was
		//serverToken in UiListState

		final WebServiceParam uiListWebServiceParams = lookupWebServiceParam(webServiceDefinition, UiListState.class);
		Assertion.checkNotNull(uiListWebServiceParams, "sort and pagination need a UiListState endpointParams. It should have been added by WebServiceParamBuilder.");

		final UiListState parsedUiListState = (UiListState) routeContext.getParamValue(uiListWebServiceParams);
		final UiListState uiListState = checkAndEnsureDefaultValue(parsedUiListState);

		String listServerToken = uiListState.getListServerToken();
		Option<DtList<?>> fullListOption = Option.empty();
		if (listServerToken != null) {
			fullListOption = tokenManager.<DtList<?>> get(uiListState.getListServerToken());
		}
		final DtList<?> fullList;
		if (fullListOption.isPresent()) {
			fullList = fullListOption.get();
		} else {
			final Object result = chain.handle(request, response, routeContext);
			Assertion.checkArgument(result instanceof DtList, "sort and pagination only supports DtList");
			fullList = (DtList<?>) result;
			listServerToken = tokenManager.put(fullList);
		}
		response.header("listServerToken", listServerToken);
		response.header("x-total-count", String.valueOf(fullList.size())); //TODO total count should be list meta
		final DtList<?> filteredList = applySortAndPagination(fullList, uiListState);
		filteredList.setMetaData(DtList.TOTAL_COUNT_META, fullList.size());
		return filteredList;
	}

	private static UiListState checkAndEnsureDefaultValue(final UiListState parsedUiListState) {
		if (parsedUiListState.getListServerToken() == null && parsedUiListState.getTop() == 0) {//check if parsedUiListState is just not initalized
			return new UiListState(DEFAULT_RESULT_PER_PAGE, parsedUiListState.getSkip(), parsedUiListState.getSortFieldName(), parsedUiListState.isSortDesc(), null);
		}
		return parsedUiListState;
	}

	/**
	 * Lookup for a parameter of the asked type, return the first found.
	 * @param webServiceDefinition WebService definition
	 * @param paramType Type asked
	 * @return first WebServiceParam of this type, null if not found
	 */
	private static WebServiceParam lookupWebServiceParam(final WebServiceDefinition webServiceDefinition, final Class<UiListState> paramType) {
		for (final WebServiceParam webServiceParam : webServiceDefinition.getWebServiceParams()) {
			if (paramType.equals(webServiceParam.getType())) {
				return webServiceParam;
			}
		}
		return null;
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final UiListState uiListState) {
		final DtList<D> sortedList;
		if (uiListState.getSortFieldName() != null) {
			sortedList = collectionsManager.createDtListProcessor()
					.sort(StringUtil.camelToConstCase(uiListState.getSortFieldName()), uiListState.isSortDesc())
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
