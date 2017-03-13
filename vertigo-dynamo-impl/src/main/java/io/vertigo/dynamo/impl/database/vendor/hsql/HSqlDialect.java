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
package io.vertigo.dynamo.impl.database.vendor.hsql;

import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;

/**
 * Dialect for HSQL databases.
 * @author mlaroche
 *
 */
public final class HSqlDialect implements SqlDialect {
	private static final String SEQUENCE_FIELD = "SEQUENCE";

	/** {@inheritDoc} */
	@Override
	public String createInsertQuery(final DtDefinition dtDefinition, final String sequencePrefix, final String tableName) {
		return new StringBuilder()
				.append("insert into ").append(tableName).append(" (")
				.append(dtDefinition.getFields()
						.stream()
						.filter(DtField::isPersistent)
						.map(DtField::getName)
						.collect(Collectors.joining(", ")))
				.append(") values (")
				.append(dtDefinition.getFields()
						.stream()
						.filter(DtField::isPersistent)
						.map(dtField -> " #DTO." + dtField.getName() + '#')
						.collect(Collectors.joining(", ")))
				.append(");")
				.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void appendMaxRows(final StringBuilder request, final Integer maxRows) {
		request.append(" and rownum() <= ").append(maxRows);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> createPrimaryKeyQuery(final String tableName, final String sequencePrefix) {
		final String sequenceName = sequencePrefix + tableName;
		final String query = new StringBuilder("select next value for ").append(sequenceName).append("  as ")
				.append(SEQUENCE_FIELD)
				.append(" from information_schema.system_sequences ")
				.append(" where ")
				.append("sequence_name = upper('").append(sequenceName).append("')")
				.toString();
		return Optional.of(query);
	}

	/** {@inheritDoc} */
	@Override
	public boolean generatedKeys() {
		return false;
	}

}
