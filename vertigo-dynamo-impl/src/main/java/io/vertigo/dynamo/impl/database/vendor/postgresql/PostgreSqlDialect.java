package io.vertigo.dynamo.impl.database.vendor.postgresql;

import java.util.stream.Collectors;

import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;

final class PostgreSqlDialect implements SqlDialect {

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
						.map(dtField -> mapField(dtField, sequencePrefix, tableName))
						.collect(Collectors.joining(", ")))
				.append(");")
				.toString();
	}

	private String mapField(final DtField dtField, final String sequencePrefix, final String tableName) {
		if (dtField.getType() != DtField.FieldType.ID) {
			return " #DTO." + dtField.getName() + '#';
		}
		return "nextval('" + sequencePrefix + tableName + "')";
	}

	/** {@inheritDoc} */
	@Override
	public void appendMaxRows(final StringBuilder request, final Integer maxRows) {
		request.append(" limit ").append(maxRows);
	}

	/** {@inheritDoc} */
	@Override
	public boolean generatedKeys() {
		return true;
	}

}
