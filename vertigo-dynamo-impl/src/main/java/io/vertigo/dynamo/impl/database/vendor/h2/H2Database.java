/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.database.vendor.h2;

import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.database.vendor.SqlExceptionHandler;
import io.vertigo.dynamo.database.vendor.SqlMapping;

/**
 * Gestion de la base de données H2.
 *
 * @author jmainaud
 */
public final class H2Database implements SqlDataBase {
	private final SqlExceptionHandler sqlExceptionHandler = new H2SqlExceptionHandler();
	private final SqlMapping sqlMapping = new H2Mapping();
	private final SqlDialect sqlDialect = new H2SqlDialect();

	/** {@inheritDoc} */
	@Override
	public SqlExceptionHandler getSqlExceptionHandler() {
		return sqlExceptionHandler;
	}

	/** {@inheritDoc} */
	@Override
	public SqlMapping getSqlMapping() {
		return sqlMapping;
	}

	@Override
	public SqlDialect getSqlDialect() {
		return sqlDialect;
	}
}
