package io.vertigo.struts2.core;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Liste de reference des couples (clé, object) enregistrés.
 * @author npiedeloup
 * @param <O> Type d'objet
 */
public final class ContextMdl<O extends DtObject> {
	private final AbstractActionSupport action;
	private final String contextKey;

	/**
	 * Constructeur.
	 * @param contextKey Clé dans le context
	 * @param action Action struts
	 */
	public ContextMdl(final String contextKey, final AbstractActionSupport action) {
		Assertion.checkArgNotEmpty(contextKey);
		Assertion.checkNotNull(action);
		//---------------------------------------------------------------------
		this.contextKey = contextKey;
		this.action = action;
	}

	/**
	 * Publie une liste de référence.
	 * @param dtObjectClass Class associée
	 * @param code Code
	 */
	public void publish(final Class<O> dtObjectClass, final String code) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtObjectClass);
		action.getModel().put(contextKey, new UiMdList<O>(new DtListURIForMasterData(dtDefinition, code)));
	}

	//	public ContextMdl(final String contextKey, final Class<O> dtoClass, final String code, final AbstractActionSupport action) {
	//		Assertion.checkArgNotEmpty(contextKey);
	//		Assertion.checkNotNull(action);
	//		//---------------------------------------------------------------------
	//		this.contextKey = contextKey;
	//		this.action = action;
	//
	//		final DtListURIForMasterData dtListURI = new DtListURIForMasterData(DtObjectUtil.findDtDefinition(dtoClass), code);
	//		final DtList<O> fkMasterDataList = persistenceManager.get().getBroker().getList(dtListURI);
	//		action.getModel().putTransient(contextKey, new UiList<O>(fkMasterDataList));
	//	}

}
