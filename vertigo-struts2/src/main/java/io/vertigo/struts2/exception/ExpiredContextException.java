package io.vertigo.struts2.exception;

/**
 * Exception lancée dans le cas ou l'on recherche un context expiré.
 * @author npiedeloup
 */
public final class ExpiredContextException extends Exception {

	private static final long serialVersionUID = 4871828055854233637L;

	/**
	 * Constructeur.
	 * @param message Message d'erreur
	 */
	public ExpiredContextException(final String message) {
		super(message);
	}
}
