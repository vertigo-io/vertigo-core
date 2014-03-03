package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation par défaut de StatementHandler.
 * Toute l'information est portée par le domain.
 * 
 * @author  pchretien
 * @version $Id: SimpleResultMetaData.java,v 1.4 2014/01/20 17:46:01 pchretien Exp $
 */
final class SimpleResultMetaData implements ResultMetaData {
	private final DtDefinition dtDefinition;
	private final boolean isDtObject;

	/**
	 * Constructeur.
	 */
	SimpleResultMetaData(final DtDefinition dtDefinition, final boolean isDtObject) {
		Assertion.checkNotNull(dtDefinition);
		//-----------------------------------------------------------------
		this.dtDefinition = dtDefinition;
		this.isDtObject = isDtObject;
	}

	/** {@inheritDoc} */
	public DtObject createDtObject() {
		return DtObjectUtil.createDtObject(dtDefinition);
	}

	/** {@inheritDoc} */
	public boolean isDtObject() {
		return isDtObject;
	}

	/** {@inheritDoc} */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}
}
