package io.vertigo.struts2.core;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.transaction.KTransactionWritable;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
final class UiMdList<D extends DtObject> extends AbstractUiList<D> implements UiList<D> {
	private static final long serialVersionUID = 5475819598230056558L;

	private final DtListURI dtListUri;
	private transient DtList<D> lazyDtList;

	/**
	 * Constructeur.
	 * @param dtListUri Uri de la Liste à encapsuler
	 */
	public UiMdList(final DtListURI dtListUri) {
		super(dtListUri.getDtDefinition());
		// -------------------------------------------------------------------------
		this.dtListUri = dtListUri;
	}

	// ==========================================================================

	/**
	 * @return Liste des données
	 */
	@Override
	public DtList<D> obtainDtList() {
		if (lazyDtList == null) {
			try (final KTransactionWritable transaction = transactionManager.get().createCurrentTransaction()) {
				lazyDtList = persistenceManager.get().getBroker().<D> getList(dtListUri);
			}
		}
		return lazyDtList;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "uiMdList(" + dtListUri.toString() + (lazyDtList != null ? ", loaded:" + lazyDtList.size() : "") + " )";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		//on surcharge equals pour eviter un appel à super.equals non d�sir� et qui forcerai le chargement de la liste
		return (o instanceof UiMdList) && dtListUri.equals(((UiMdList<?>) o).dtListUri);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		//on surcharge hashCode pour eviter un appel à super.hashCode non d�sir� et qui forcerai le chargement de la liste
		return dtListUri.hashCode();
	}

	/** {@inheritDoc} */
	public DtList<D> validate(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		return obtainDtList();
	}

	/** {@inheritDoc} */
	public void check(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		//rien
	}

	/** {@inheritDoc} */
	public DtList<D> flush() {
		return obtainDtList();
	}
}
