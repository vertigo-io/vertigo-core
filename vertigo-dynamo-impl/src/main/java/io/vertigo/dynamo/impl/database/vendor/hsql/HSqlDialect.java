package io.vertigo.dynamo.impl.database.vendor.hsql;

import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.TaskEngineProc;

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
	public void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(separator).append(" rownum() <= ").append(maxRows);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> preparePrimaryKey(final Entity entity, final String tableName, final String sequencePrefix) {
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
	public Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return TaskEngineProc.class;
	}
}
