/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.lang.Assertion;

/**
 * this class concerns the param of a sql statement.
 *
 * @author pchretien
 */
final class SqlParameter<O> {
	private final Class<O> dataType;
	private final boolean in;
	private final O value;

	/**
	 * Constructor.
	 * @param dataType the param type
	 * @param in if the param is an input (or an output)
	 */
	SqlParameter(final Class<O> dataType, final O value, final boolean in) {
		Assertion.checkNotNull(dataType);
		//---
		this.dataType = dataType;
		this.in = in;
		this.value = value;
	}

	/**
	 * @return if the param is an input
	 */
	boolean isIn() {
		return in;
	}

	/**
	 * @return if the param is an output
	 */
	boolean isOut() {
		return !in;
	}

	/**
	 * @return the param type
	 */
	Class getDataType() {
		return dataType;
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(in ? "in" : "out")
				.append('=')
				.append(value != null ? value : "null")
				.toString();
	}
}
