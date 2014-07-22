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
package io.vertigo.rest.handler;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.rest.engine.UiListState;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;

import javax.inject.Inject;
import javax.inject.Named;

import spark.Request;
import spark.Response;

/**
 * Auto paginator and Sort handler.
 * @author npiedeloup
 */
final class PaginatorAndSortHandler implements RouteHandler {
	private static final long DEFAULT_RESULT_PER_PAGE = 20;
	private final CollectionsManager collectionsManager;
	private final long resultPerPage;

	/**
	 * Constructor.
	 * @param windowSeconds the time windows use to limit calls rate
	 * @param limitValue the rate limit ceiling value
	 * @param securityManager Security Manager
	 */
	@Inject
	public PaginatorAndSortHandler(@Named("resultPerPage") final Option<Long> resultPerPage, final CollectionsManager collectionsManager) {
		Assertion.checkNotNull(collectionsManager);
		//---------------------------------------------------------------------
		this.collectionsManager = collectionsManager;
		this.resultPerPage = resultPerPage.getOrElse(DEFAULT_RESULT_PER_PAGE);
	}

	/** {@inheritDoc}  */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		final Object result = chain.handle(request, response, routeContext);
		Assertion.checkArgument(result instanceof DtList, "sort and pagination only supports DtList");

		if (result instanceof DtList) {
			final UiListState uiListState = readUiListState(request);
			final DtList<?> filteredList = applySortAndPagination((DtList) result, uiListState);
			return filteredList;
		}
		return result;
	}

	private UiListState readUiListState(final Request request) {
		return null;
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final UiListState uiListState) {
		final DtList<D> sortedList;
		if (uiListState.getSortFieldName() != null) {
			final DtListFunction<D> sortFunction = collectionsManager.createSort(uiListState.getSortFieldName(), uiListState.isSortDesc(), true, true);
			sortedList = sortFunction.apply(unFilteredList);
		} else {
			sortedList = unFilteredList;
		}
		final DtList<D> filteredList;
		if (uiListState.getTop() > 0) {
			final DtListFunction<D> filterFunction = collectionsManager.createFilterSubList(uiListState.getSkip(), uiListState.getSkip() + uiListState.getTop());
			filteredList = filterFunction.apply(sortedList);
		} else {
			filteredList = sortedList;
		}
		return filteredList;
	}

}
