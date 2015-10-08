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
package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * Single Expression Definition.
 * (preBody)(field|multiField):(query)(postBody)
 * @author npiedeloup
 */
public final class DslExpressionDefinition {
	private final String preBody; //Spaces like
	private final Option<DslFieldDefinition> field;
	private final Option<DslMultiFieldDefinition> multiField;

	private final DslQueryDefinition query;
	private final String postBody; //Spaces like

	/**
	 * @param preBody String before body
	 * @param field Optional fieldDefinition
	 * @param multiField Optional multiFieldDefinition
	 * @param query QueryDefinition
	 * @param postBody String after body
	 */
	public DslExpressionDefinition(final String preBody,
			final Option<DslFieldDefinition> field, final Option<DslMultiFieldDefinition> multiField,
			final DslQueryDefinition query,
			final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(field);
		Assertion.checkNotNull(multiField);
		Assertion.checkNotNull(query);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.field = field;
		this.multiField = multiField;
		this.query = query;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder()
				.append(preBody);
		if (field.isDefined()) {
			sb.append(field.get())
					.append(":");
		}
		if (multiField.isDefined()) {
			sb.append(multiField.get())
					.append(":");
		}
		sb.append(query)
				.append(postBody);
		return sb.toString();
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return optional Field
	 */
	public final Option<DslFieldDefinition> getField() {
		return field;
	}

	/**
	 * @return optional MultiField
	 */

	public final Option<DslMultiFieldDefinition> getMultiField() {
		return multiField;
	}

	/**
	 * @return query
	 */
	public final DslQueryDefinition getQuery() {
		return query;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}

}
