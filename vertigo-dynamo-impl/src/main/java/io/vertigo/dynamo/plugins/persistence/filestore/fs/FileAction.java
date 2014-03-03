/**
 * 
 */
package io.vertigo.dynamo.plugins.persistence.filestore.fs;

/**
 * Interface d'action sur un fichier.
 * 
 * @author skerdudou
 * @version $Id: FileAction.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
interface FileAction {

	static enum State {
		/** Etat d'initialisation */
		READY,
		/** Etat après l'action process() */
		PROCESSED,
		/** Etat après clean */
		END,
		/** Etat d'erreeur */
		ERROR
	}

	/**
	 * Effectue l'action demandée sur le fichier.
	 * 
	 * @throws Exception Si impossible
	 */
	void process() throws Exception;

	/**
	 * Supprime les fichiers temporaires, les informations d'annulation et vide l'action.
	 */
	void clean();

	/**
	 * récupère le chemin complet du fichier. Ceci est nécessaire afin de retirer des inserts sur ce fichier et qui
	 * serait inutile au commit.
	 * 
	 * @return chemin du fichier
	 */
	String getAbsolutePath();
}
