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
package io.vertigo.vega.webservice.model;

import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.lang.Assertion;

import java.io.Serializable;

/**
 * List state : page and sort infos.
 * @author npiedeloup (22 juil. 2014 17:31:15)
 */
public final class UiListState implements Serializable {
	private static final long serialVersionUID = 1343925518488701786L;
	private final int top;
	private final int skip;
	private final String sortFieldName;
	private final boolean sortDesc;

	//reference the previous serverToken of the list. Use to sort and paginate a snapshot of list.
	private final String listServerToken;

	/**
	 * @param top max returning elements
	 * @param skip elements to skip
	 * @param sortFieldName sort fieldName
	 * @param sortDesc desc or asc order
	 * @param listServerToken reference the previous serverToken of the list
	 */
	public UiListState(final int top, final int skip, final String sortFieldName, final boolean sortDesc, final String listServerToken) {
		Assertion.checkArgument(top > 0, "Top must be positive ({0})", top);
		Assertion.checkArgument(skip >= 0, "Skip must be positive ({0})", skip);
		//-----
		this.top = top;
		this.skip = skip;
		this.sortFieldName = sortFieldName;
		this.sortDesc = sortDesc;
		this.listServerToken = listServerToken;
	}

	/**
	 * @return nb returning elements
	 */
	public int getTop() {
		return top;
	}

	/**
	 * @return nb elements to skip
	 */
	public int getSkip() {
		return skip;
	}

	/**
	 * @return sort fieldName
	 */
	public String getSortFieldName() {
		return sortFieldName;
	}

	/**
	 * @return  desc or asc order
	 */
	public boolean isSortDesc() {
		return sortDesc;
	}

	/**
	 * @return serverToken
	 */
	public String getListServerToken() {
		return listServerToken;
	}

	/**
	 * @return Extract DtListState from this UiListState
	 */
	public DtListState toDtListState() {
		return new DtListState(getTop(), getSkip(), getSortFieldName(), isSortDesc());
	}
}
