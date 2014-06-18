package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.kernel.lang.MessageText;

/**
 * Exception lancée en cas d'échec de vérification des contraintes.
 *
 * @author  pchretien
 */
public final class ConstraintException extends Exception {
	private static final long serialVersionUID = -7317938262923785124L;
	private final MessageText messageText;

	/**
	 * Constructeur.
	 * @param messageText MessageText de l'erreur.
	 */
	public ConstraintException(final MessageText messageText) {
		super(messageText.getDisplay());
		this.messageText = messageText;
	}

	/**
	 * Gestion des messages d'erreur externalisés.
	 * @return messageText.
	 */
	public MessageText getMessageText() {
		return messageText;
	}
}
