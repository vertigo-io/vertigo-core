package io.vertigo.publisher.impl.merger.processor;

/**
 * Handler du parsing XML.
 * @author npiedeloup
 * @version $Id: ParserXMLHandler.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public interface ParserXMLHandler {
	/**
	 * Appell� sur un tag sans body.
	 * @param tagXML Extrait du tag XML complet
	 * @param output Flux d'ecriture, il contient tout jusqu'au caract�re pr�c�dant le tag lui m�me
	 */
	void onNoBodyEndTag(final String tagXML, final StringBuilder output);

	/**
	 * Appell� sur un tag sans body.
	 * @param tagXML Extrait du tag XML complet (ie : avec son Body)
	 * @param bodyContent Body du tag
	 * @param output Flux d'ecriture, il contient tout jusqu'au caract�re pr�c�dant le tag lui m�me
	 */
	void onBodyEndTag(final String tagXML, final String bodyContent, final StringBuilder output);

}