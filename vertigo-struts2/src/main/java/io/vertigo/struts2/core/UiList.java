package io.vertigo.struts2.core;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.util.List;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
public interface UiList<D extends DtObject> extends List<UiObject<D>> {

	/**
	 * Vérifie les UiObjects de la liste, met à jour les objets métiers et retourne la liste.
	 * @param validator Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Liste métier valid�e.
	 */
	DtList<D> validate(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack);

	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param validator Validateur à utilisé
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 */
	void check(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack);

	/**
	 * @return met à jour les objets métiers et retourne la liste.
	 */
	DtList<D> flush();

}
