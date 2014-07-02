package io.vertigo.studio.plugins.reporting.task.metrics.explainplan;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.studio.plugins.reporting.task.metrics.performance.TaskPopulator;
import io.vertigo.studio.reporting.MetricEngine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Plugin qui va lancer la commande de calcul puis d'affichage du plan d'exécution.
 * 
 * @author tchassagnette
 */
public final class ExplainPlanMetricEngine implements MetricEngine<TaskDefinition, ExplainPlanMetric> {
	private int sequence;

	private final WorkManager workManager;

	/**
	 * Constructeur apr défaut.
	 * @param workManager Manager des works
	 */
	public ExplainPlanMetricEngine(final WorkManager workManager) {
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
	}

	/** {@inheritDoc} */
	public ExplainPlanMetric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---------------------------------------------------------------------
		try {
			if (TaskEngineSelect.class.isAssignableFrom(getTaskEngineClass(taskDefinition))) {
				final int currentSequence = sequence++;
				final String explainPlan = getExplainPlanElement(taskDefinition, currentSequence);
				return new ExplainPlanMetric(explainPlan);
			}

			return new ExplainPlanMetric();
		} catch (final Throwable e) {
			e.printStackTrace();
			return new ExplainPlanMetric(e);
		}
	}

	private String getExplainPlanElement(final TaskDefinition taskDefinition, final int currentSequence) {
		final String taskDefinitionName = truncate("TK_EXPLAIN_" + taskDefinition.getName(), 59, "_");
		final String explainPlanRequest = "explain plan set statement_id = 'PLAN_" + currentSequence + "' for " + taskDefinition.getRequest();
		//final String explainPlanRequest = "explain plan for " + taskDefinition.getRequest();

		final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskDefinitionName)//
				.withEngine(getTaskEngineClass(taskDefinition))//
				.withRequest(explainPlanRequest)//
				.withPackageName(getClass().getPackage().getName());
		for (final TaskAttribute attribute : taskDefinition.getAttributes()) {
			if (attribute.isIn()) {
				taskDefinitionBuilder.withAttribute(attribute.getName(), attribute.getDomain(), attribute.isNotNull(), attribute.isIn());
			} else {
				taskDefinitionBuilder.withAttribute(attribute.getName(), attribute.getDomain(), false, false);
			}
		}
		final TaskDefinition taskExplain = taskDefinitionBuilder.build();
		try {
			final Task currentTask = new TaskPopulator(taskExplain).populateTask();
			WorkItem<TaskResult, Task> workItem = new WorkItem<>(currentTask, taskExplain.getTaskEngineProvider());
			workManager.process(workItem);
			//On n'exploite pas le résultat
			return readExplainPlan(taskDefinition, currentSequence);
		} catch (final Exception e) {
			throw new VRuntimeException("explainPlanElement", e);
		}
	}

	private Class<? extends TaskEngine> getTaskEngineClass(final TaskDefinition taskDefinition) {
		return (Class<? extends TaskEngine>) ClassUtil.classForName(taskDefinition.getTaskEngineProvider().getName());
	}

	private static String truncate(final String value, final int maxSize, final String endTruncString) {
		if (value.length() <= maxSize) {
			return value;
		}
		return value.substring(0, maxSize - endTruncString.length()) + endTruncString;
	}

	private String readExplainPlan(final TaskDefinition taskDefinition, final int currentSequence) {
		//		final String taskDefinitionName = "TK_EXPLAIN_PLAN";
		//		final TaskDefinitionFactory taskDefinitionFactory = getTaskManager().createTaskDefinitionFactory();
		//		final String explainPlanRequest = "select * from plan_table where statement_id = #STATEMENT_ID# order by id";
		//		taskDefinitionFactory.init(taskDefinitionName, TaskEngineSelect.class, explainPlanRequest, TaskEngineSelect.class.getPackage().getName());
		//		taskDefinitionFactory.addAttribute(taskDefinitionName, "STATEMENT_ID", getDomainManager().getNameSpace().getDomain("DO_LIBELLE_LONG"), true, true);
		//		
		//		final TaskDefinition taskExplain = taskDefinitionFactory.createTaskDefinition(taskDefinitionName);
		//		try {
		//			final Task currentTask = getTaskManager().getFactory().createTask(taskExplain);
		//			currentTask.setValue("STATEMENT_ID", taskDefinition.getShortName());
		//			getWorkManager().process(currentTask);
		//			return new ExplainPlanMetric();
		//		} catch (final Exception e) {
		//			throw new KSystemException("explainPlanElement", e);
		//		}
		final StringBuilder sb = new StringBuilder();
		final String sql = "SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY('PLAN_TABLE', 'PLAN_" + currentSequence + "'))";
		//final String sql = "SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY())";
		final KConnection kConnection = getCurrentConnection();
		final Connection connection = kConnection.getJdbcConnection();
		try (final PreparedStatement statement = connection.prepareStatement(sql)) {
			try (final ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					sb.append(resultSet.getString(1)).append("\n");
				}
				return sb.toString();
			}

		} catch (final SQLException e) {
			throw new VRuntimeException("doGetExplainPlan", e);
		}
	}

	/**
	 * Retourne la connexion SQL de cette transaction en la demandant au pool de connexion si nécessaire.
	 * @return Connexion SQL
	 */
	private KConnection getCurrentConnection() {
		final KTransaction transaction = Home.getComponentSpace().resolve(KTransactionManager.class).getCurrentTransaction();
		return transaction.getResource(AbstractTaskEngineSQL.SQL_RESOURCE_ID);
	}

}
