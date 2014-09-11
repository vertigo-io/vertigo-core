package io.vertigo.struts2.core;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
public final class UiListUnmodifiable<D extends DtObject> extends AbstractUiList<D> implements UiList<D> {
	private static final long serialVersionUID = 5475819598230056558L;

	private final DtList<D> dtList;

	/**
	 * Constructeur.
	 * @param dtList Liste à encapsuler
	 */
	public UiListUnmodifiable(final DtList<D> dtList) {
		super(dtList.getDefinition());
		// -------------------------------------------------------------------------
		this.dtList = dtList;
		if (dtList.size() < 1000) {
			initUiObjectByIdIndex();
		}
	}

	// ==========================================================================

	/** {@inheritDoc} */
	@Override
	protected DtList<D> obtainDtList() {
		return dtList;
	}

	/**
	 * Vérifie les UiObjects de la liste, met à jour les objets métiers et retourne la liste.
	 * @param validator Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Liste métier valid�e.
	 */
	public DtList<D> validate(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		check(validator, uiMessageStack);
		return flush();
	}

	/**
	 * @param validator
	 * @param action
	 * @param contextKey
	 */
	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param validator Validateur à utilisé
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 */
	public void check(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		//1. check Error => KUserException
		//on valide les éléments internes
		for (final UiObject<D> uiObject : getUiObjectBuffer()) {
			uiObject.check(validator, uiMessageStack);
		}
	}

	/**
	 * @return met à jour les objets métiers et retourne la liste.
	 */
	public DtList<D> flush() {
		//1. check Error => KUserException
		//on valide les éléments internes
		for (final UiObject<D> dtoInput : getUiObjectBuffer()) {
			dtoInput.flush();
		}
		clearUiObjectBuffer(); //on purge le buffer
		return dtList;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("uiList(" + dtList.size() + " element(s)");
		for (int i = 0; i < Math.min(dtList.size(), 50); i++) {
			sb.append("; ");
			sb.append(get(i));
		}
		sb.append(")");
		return sb.toString();
	}
}
