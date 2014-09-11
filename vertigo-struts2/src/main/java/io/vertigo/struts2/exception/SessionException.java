package io.vertigo.struts2.exception;

/**
 * Exception lancée dans le cas ou la session expir�e.
 * @author npiedeloup
 */
public final class SessionException extends Exception {

	private static final long serialVersionUID = 4871828055854233637L;

	/**
	 * Constructeur.
	 * @param message Message d'erreur
	 */
	public SessionException(final String message) {
		super(message);
	}
}
