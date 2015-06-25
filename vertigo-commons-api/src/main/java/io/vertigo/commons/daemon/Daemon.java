package io.vertigo.commons.daemon;

/**
 * Interface sémantique différenciant les daemons des simples runnables.
 *
 * @author TINGARGIOLA
 */
public interface Daemon {

	/**
	 * Runnalbe with exception...
	 *
	 * @throws Exception exception.
	 */
	void run() throws Exception;
}
