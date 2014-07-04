package io.vertigo.dynamo.plugins.persistence.oracle;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.plugins.persistence.AbstractSQLStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation d'un Store Oracle.
 * Dans le cas de Oracle, la gestion des clés est assurée par des séquences.
 *
 * @author pchretien
 */
public final class OracleStorePlugin extends AbstractSQLStorePlugin {
	private final String sequencePrefix;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 * @param sequencePrefix Configuration du préfixe de la séquence
	 */
	@Inject
	public OracleStorePlugin(@Named("sequencePrefix") final String sequencePrefix, final TaskManager taskManager) {
		super(taskManager);
		Assertion.checkArgNotEmpty(sequencePrefix);
		//---------------------------------------------------------------------
		this.sequencePrefix = sequencePrefix;
	}

	/**
	 * Nom de la séquence utilisée lors des inserts
	 * @param dtDefinition Définition du DT mappé
	 * @return String Nom de la sequence
	 */
	private String getSequenceName(final DtDefinition dtDefinition) {
		//oracle n'autorise pas de sequence de plus de 30 char.
		String seqName = sequencePrefix + getTableName(dtDefinition);
		if (seqName.length() > 30) {
			seqName = seqName.substring(0, 30);
		}
		return seqName;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return TaskEngineProc.class;
	}

	/** {@inheritDoc} */
	@Override
	protected String createInsertQuery(final DtDefinition dtDefinition) {
		final String tableName = getTableName(dtDefinition);
		final DtField pk = dtDefinition.getIdField().get();
		final StringBuilder request = new StringBuilder();

		request.append("begin insert into ").append(tableName).append(" ( ");
		String separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator);
				request.append(dtField.getName());
				separator = ", ";
			}
		}
		request.append(") values ( ");
		separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator);
				if (dtField.getType() != DtField.FieldType.PRIMARY_KEY) {
					request.append(" #DTO.").append(dtField.getName()).append('#');
				} else {
					request.append(getSequenceName(dtDefinition)).append(".nextval ");
				}
				separator = ", ";
			}
		}
		request.append(") returning ").append(pk.getName()).append(" into %DTO.").append(pk.getName()).append("%;");
		request.append("end;");
		return request.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(separator).append(" rownum <= ").append(maxRows.toString());
	}
}
