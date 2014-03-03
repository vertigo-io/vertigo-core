package io.vertigo.dynamo.plugins.persistence.sqlserver;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.plugins.persistence.AbstractSQLStorePlugin;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.sqlserver.TaskEngineInsertWithGeneratedKeys;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import javax.inject.Inject;

/**
 * Implémentation d'un Store MS Sql Server.
 * Dans le cas de SQL Server, la gestion des clés n'est pas assurée par des séquences.
 *
 * @author  jmainaud, evernat
 * @version $Id: SqlServerStorePlugin.java,v 1.5 2014/01/20 18:57:19 pchretien Exp $
 */
public final class SqlServerStorePlugin extends AbstractSQLStorePlugin {
	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 */
	@Inject
	public SqlServerStorePlugin(final WorkManager workManager) {
		super(workManager);
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		Assertion.checkArgument(request.indexOf("select ") == 0, "request doit commencer par select");
		//---------------------------------------------------------------------
		request.insert("select ".length(), " top " + maxRows + ' ');
	}

	/** {@inheritDoc} */
	@Override
	protected void postAlterLoadRequest(final StringBuilder request) {
		//dans le cas de SQLServer, il faut rajouter un SET TEXTSIZE XX sinon les select * sont très lent, car le contenu complet du blob est retourné.
		//TODO Normalement il ne faudrait le rajouter que si il y a une colonne type LargeObject (image, text, ntext, varbinary(max))
		final String convertStrConctat = StringUtil.replace(request.toString(), " || ", " + ");
		request.setLength(0);
		//	request.append(" SET TEXTSIZE 16\n ");//16 correspond à la taille du pointeur.
		request.append(convertStrConctat);
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return insert ? TaskEngineInsertWithGeneratedKeys.class : TaskEngineProc.class;
	}

	/** {@inheritDoc} */
	@Override
	protected String createInsertQuery(final DtDefinition dtDefinition) {
		final String tableName = getTableName(dtDefinition);
		final StringBuilder request = new StringBuilder();
		request.append("insert into ");
		request.append(tableName);
		request.append(" ( ");

		String separator = "";

		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent() && dtField.getType() != DtField.FieldType.PRIMARY_KEY) {
				request.append(separator);
				request.append(dtField.getName());
				separator = ", ";
			}
		}

		request.append(") values ( ");
		separator = "";

		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent() && dtField.getType() != DtField.FieldType.PRIMARY_KEY) {
				request.append(separator);
				request.append(" #DTO.");
				request.append(dtField.getName());
				request.append('#');
				separator = ", ";
			}
		}

		request.append(") ");
		return request.toString();
	}
}
