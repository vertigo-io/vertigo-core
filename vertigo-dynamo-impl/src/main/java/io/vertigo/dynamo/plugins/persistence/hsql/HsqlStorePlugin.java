package io.vertigo.dynamo.plugins.persistence.hsql;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.plugins.persistence.AbstractSQLStorePlugin;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation d'un Store HSQLDB.
 * Dans le cas de HSQLDB, la gestion des clés est assurée par des séquences.
 *
 * @author  pchretien
 */
public final class HsqlStorePlugin extends AbstractSQLStorePlugin {
	private static final String DTO_SEQUENCE = "DTO_SEQUENCE";
	private static final String SEQUENCE_FIELD = "SEQUENCE";
	/**
	 * Prefix de la tache : SELECT
	 */
	private static final String TK_SELECT = "TK_SELECT";

	/**
	 * Domaine à usage interne.
	 * Ce domaine n'est pas enregistré.
	 */
	private final Domain resultDomain = new Domain("DO_HSQL", DataType.DtObject, new FormatterDefault("FMT_DEFAULT"));
	private final String sequencePrefix;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 * @param sequencePrefix Configuration du préfixe de la séquence
	 */
	@Inject
	public HsqlStorePlugin(@Named("sequencePrefix") final String sequencePrefix, final WorkManager workManager) {
		super(workManager);
		Assertion.checkArgNotEmpty(sequencePrefix);
		//---------------------------------------------------------------------
		this.sequencePrefix = sequencePrefix;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return TaskEngineProc.class;
	}

	private Long getSequenceNextval(final String sequenceName) {
		final String taskName = TK_SELECT + '_' + sequenceName;

		final StringBuilder request = chooseDataBaseStyle(sequenceName);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)//
				.withEngine(TaskEngineSelect.class)//
				.withRequest(request.toString())//
				.withAttribute(DTO_SEQUENCE, resultDomain, true, false)// OUT, obligatoire
				.build();

		final Task task = createTaskBuilder(taskDefinition).build();
		final TaskResult taskResult = process(task);
		final DtObject dto = taskResult.getValue(DTO_SEQUENCE);
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(SEQUENCE_FIELD);

		return Long.valueOf((Integer) dtField.getDataAccessor().getValue(dto));
	}

	private static StringBuilder chooseDataBaseStyle(final String sequenceName) {
		return new StringBuilder("select next value for " + sequenceName + "  as " + SEQUENCE_FIELD)//
				.append(" from information_schema.system_sequences where sequence_name = upper('" + sequenceName + "')");
	}

	/** {@inheritDoc} */
	@Override
	protected void preparePrimaryKey(final DtObject dto) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField pk = dtDefinition.getIdField().get();
		pk.getDataAccessor().setValue(dto, getSequenceNextval(sequencePrefix + getTableName(dtDefinition)));
		//			executeInsert(transaction, dto);
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
				request.append(" #DTO.").append(dtField.getName()).append('#');
				separator = ", ";
			}
		}
		request.append(");");
		return request.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(separator).append(" rownum <= ").append(maxRows.toString());
	}
}
