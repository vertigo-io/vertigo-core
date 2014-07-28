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
package io.vertigo.vega.rest.engine;

import io.vertigo.dynamo.domain.model.DtObject;

public class OData<O extends DtObject> {

	private final O filter;

	private final long top;
	private final long skip;
	private final String orderBy;
	private final String format;
	private final long inlineCount;
	private final long totalCount;

	public OData(O filter, long top, long skip, String orderBy, String format, long inlineCount, long totalCount) {
		this.filter = filter;
		this.top = top;
		this.skip = skip;
		this.orderBy = orderBy;
		this.format = format;
		this.inlineCount = inlineCount;
		this.totalCount = totalCount;
	}

	public O getFilter() {
		return filter;
	}

	public long getTop() {
		return top;
	}

	public long getSkip() {
		return skip;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public String getFormat() {
		return format;
	}

	public long getInlineCount() {
		return inlineCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

}
