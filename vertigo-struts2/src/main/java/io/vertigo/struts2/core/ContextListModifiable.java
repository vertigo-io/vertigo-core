package io.vertigo.struts2.core;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Liste des couples (clé, object) enregistrés.
 * @author npiedeloup
 * @param <O> Type d'objet
 */
public final class ContextListModifiable<O extends DtObject> {
	private final AbstractActionSupport action;
	private final UiMessageStack uiMessageStack;
	private final String contextKey;
	private final UiObjectValidator validator;

	//	public static <O extends DtObject> ContextForm<O> create(final String contextKey, final AbstractActionSupport action) {
	//		return new ContextForm<O>(contextKey, action);
	//	}

	/**
	 * Constructeur.
	 * @param contextKey Clé dans le context
	 * @param action Action struts
	 */
	public ContextListModifiable(final String contextKey, final AbstractActionSupport action) {
		this(contextKey, new UiObjectValidator(), action);
	}

	/**
	 * Constructeur.
	 * @param contextKey Clé dans le context
	 * @param validator Validator a utiliser
	 * @param action Action struts
	 */
	public ContextListModifiable(final String contextKey, final UiObjectValidator validator, final AbstractActionSupport action) {
		Assertion.checkArgNotEmpty(contextKey);
		Assertion.checkNotNull(action);
		Assertion.checkNotNull(validator);
		//---------------------------------------------------------------------
		this.contextKey = contextKey;
		this.action = action;
		this.uiMessageStack = action.getUiMessageStack();
		this.validator = validator;
	}

	/**
	 * Ajoute une liste au context.
	 * @param dtList List à publier
	 */
	public void publish(final DtList<O> dtList) {
		action.getModel().put(contextKey, new UiListModifiable<>(dtList));
	}

	/**
	 * Vérifie les erreurs de la liste. Celles-ci sont ajoutées à l'uiMessageStack si nécessaire.
	 */
	public void checkErrors() {
		action.getModel().getUiList(contextKey).check(validator, uiMessageStack);
	}

	/**
	 * @return List des objets métiers valid�e. Lance une exception si erreur.
	 */
	public DtList<O> readDtList() {
		return action.getModel().<O> getUiList(contextKey).validate(validator, uiMessageStack);
	}

	/**
	 * @return List des objets d'IHM. Peut contenir des erreurs.
	 */
	public UiList<O> getUiList() {
		return action.getModel().getUiList(contextKey);
	}

	private UiListModifiable<O> getUiListModifiable() {
		return action.getModel().getUiListModifiable(contextKey);
	}

	/**
	 * @return List des objets supprim�s
	 */
	public DtList<O> getRemovedList() {
		return getUiListModifiable().getRemovedList();
	}

	/**
	 * @return List des objets ajoutés
	 */
	public DtList<O> getAddedList() {
		return getUiListModifiable().getAddedList();
	}

	/**
	 * @return List des objets modifiés
	 */
	public DtList<O> getModifiedList() {
		return getUiListModifiable().getModifiedList();
	}

}
