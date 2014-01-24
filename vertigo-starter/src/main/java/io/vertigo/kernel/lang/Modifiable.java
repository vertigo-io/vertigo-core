package io.vertigo.kernel.lang;

/**
 * Comportement transverse permettant de d�crire des objets modifiables devenant non modifiable.
 *  
 * @author pchretien
 * @version $Id: Modifiable.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface Modifiable {
	/**
	 * Rend l'objet non modifiable, non mutable.
	 */
	void makeUnmodifiable();

	/**
	 * @return Si l'objet peut �tre modif�e.
	 */
	boolean isModifiable();
}
