package io.vertigo.quarto.publisher.impl.merger.processor;

import io.vertigo.quarto.publisher.model.PublisherData;

import java.io.IOException;

/**
 * Interface d'un processor pour le reportMerger.
 * Ce processor � une entr�e et une sortie de meme type,
 * la sortie est issus d'un traitement prenant une entr�e et les parametres du merger.
 * @author npiedeloup
 * @version $Id: MergerProcessor.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public interface MergerProcessor {
	/**
	 * Utilise la chaine d'entr�e et les parametres de merge pour faire un traitement sp�cifique.
	 * 
	 * Attention la valeur du input peut avoir �t� mut�e.
	 * @param input Donn�e d'entr�e.
	 * @param publisherData Donn�es de la fusion d'�dition
	 * @return Chaine manipul�e par le processor
	 * @throws IOException Erreur I/O
	 */
	String execute(String input, final PublisherData publisherData) throws IOException;
}
