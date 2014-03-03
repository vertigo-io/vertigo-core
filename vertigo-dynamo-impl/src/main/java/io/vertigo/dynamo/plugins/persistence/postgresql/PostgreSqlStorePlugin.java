package io.vertigo.dynamo.plugins.persistence.postgresql;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.plugins.persistence.AbstractSQLStorePlugin;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.sqlserver.TaskEngineInsertWithGeneratedKeys;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation d'un Store PostgreSQL.
 *
 * @author  pchretien
 * @version $Id: PostgreSqlStorePlugin.java,v 1.5 2014/01/20 18:57:19 pchretien Exp $
 */
public final class PostgreSqlStorePlugin extends AbstractSQLStorePlugin {
	private final String sequencePrefix;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 * @param sequencePrefix Configuration du préfixe de la séquence
	 */
	@Inject
	public PostgreSqlStorePlugin(@Named("sequencePrefix") final String sequencePrefix, final WorkManager workManager) {
		super(workManager);
		Assertion.checkArgNotEmpty(sequencePrefix);
		//---------------------------------------------------------------------
		this.sequencePrefix = sequencePrefix;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return insert ? TaskEngineInsertWithGeneratedKeys.class : TaskEngineProc.class;
	}

	private String getSequenceName(final DtDefinition dtDefinition) {
		return sequencePrefix + getTableName(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	protected String createInsertQuery(final DtDefinition dtDefinition) {
		final String tableName = getTableName(dtDefinition);
		final StringBuilder request = new StringBuilder();
		request.append("insert into ").append(tableName).append(" (");
		String separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator);
				request.append(dtField.getName());
				separator = ", ";
			}
		}
		request.append(") values (");
		separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator);
				if (dtField.getType() != DtField.FieldType.PRIMARY_KEY) {
					request.append(" #DTO.").append(dtField.getName()).append('#');
				} else {
					request.append("nextval('").append(getSequenceName(dtDefinition)).append("')");
				}
				separator = ", ";
			}
		}
		request.append(");");
		return request.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(" limit ").append(maxRows.toString());
	}
}
