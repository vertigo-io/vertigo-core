package io.vertigo.dynamo.impl.environment;

/**
 * Exception lors du chargement par un LoaderPlugin.
 * @author npiedeloup
 */
public final class LoaderException extends Exception {
	private static final long serialVersionUID = -8554090427385680300L;

	/**
	 * Constructeur.
	 * @param msg Message de l'exception
	 * @param e Cause de l'exception (peut être null)
	 */
	public LoaderException(final String msg, final Exception e) {
		super(msg, e);
	}
}
