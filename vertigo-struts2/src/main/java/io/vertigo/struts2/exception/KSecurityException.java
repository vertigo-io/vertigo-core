package io.vertigo.struts2.exception;

/**
 * Exception de sécurité.
 * @author npiedeloup
 */
public final class KSecurityException extends Exception {

	private static final long serialVersionUID = -8681804137431091875L;

	/**
	 * Constructeur.
	 * @param message Message d'erreur
	 */
	public KSecurityException(final String message) {
		super(message);
	}

}
