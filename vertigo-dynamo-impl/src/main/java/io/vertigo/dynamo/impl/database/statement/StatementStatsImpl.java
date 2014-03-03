package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.kernel.lang.Assertion;

/**
* Class de statistique pour le suivi des traitements SQL.
*
* @author npiedeloup
* @version $Id: StatementStatsImpl.java,v 1.3 2013/10/22 10:43:35 pchretien Exp $
*/
final class StatementStatsImpl implements StatementStats {
	private final KPreparedStatement statement;
	private long elapsedTime = -1; //non renseigné
	private boolean success; //false par défaut
	private Long nbModifiedRow;
	private Long nbSelectedRow;

	StatementStatsImpl(final KPreparedStatement statement) {
		Assertion.checkNotNull(statement);
		//---------------------------------------------------------------------
		this.statement = statement;
	}

	/** {@inheritDoc} */
	public long getElapsedTime() {
		return elapsedTime;
	}

	void setElapsedTime(final long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/** {@inheritDoc} */
	public Long getNbModifiedRow() {
		return nbModifiedRow;
	}

	void setNbModifiedRow(final long nbModifiedRow) {
		this.nbModifiedRow = nbModifiedRow;
	}

	/** {@inheritDoc} */
	public Long getNbSelectedRow() {
		return nbSelectedRow;
	}

	void setNbSelectedRow(final long nbSelectedRow) {
		this.nbSelectedRow = nbSelectedRow;
	}

	/** {@inheritDoc} */
	public KPreparedStatement getPreparedStatement() {
		return statement;
	}

	/** {@inheritDoc} */
	public boolean isSuccess() {
		return success;
	}

	void setSuccess(final boolean success) {
		this.success = success;
	}
}
