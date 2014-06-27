package io.vertigo.rest.exception;

/**
 * Exception for expired session.
 * @author npiedeloup
 */
public final class SessionException extends Exception {

	private static final long serialVersionUID = 4871828055854233637L;

	/**
	 * Constructor.
	 * @param message Error message
	 */
	public SessionException(final String message) {
		super(message);
	}
}
