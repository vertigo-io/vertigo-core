package io.vertigo.account.authorization;

import io.vertigo.core.locale.MessageText;

/**
 * Security exception.
 * @author npiedeloup
 */
public final class VSecurityException extends RuntimeException {
	private static final long serialVersionUID = 3911465988816189879L;
	private final MessageText messageText;

	/**
	 * Constructor.
	 * @param messageText Message de l'exception
	 */
	public VSecurityException(final MessageText messageText) {
		//Attention il convient d'utiliser une méthode qui ne remonte d'exception.
		super(messageText.getDisplay());
		// On rerentre sur l'API des Exception en passant le message.
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
