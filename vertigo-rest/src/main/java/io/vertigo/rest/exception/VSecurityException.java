package io.vertigo.rest.exception;

/**
 * Security exception.
 * @author npiedeloup
 */
public final class VSecurityException extends Exception {

	private static final long serialVersionUID = -8681804137431091875L;

	/**
	 * Constructor.
	 * @param message Error message
	 */
	public VSecurityException(final String message) {
		super(message);
	}

}
