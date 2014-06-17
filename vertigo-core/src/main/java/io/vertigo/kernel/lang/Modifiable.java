package io.vertigo.kernel.lang;

/**
 * Comportement transverse permettant de décrire des objets modifiables devenant non modifiable.
 *  
 * @author pchretien
 */
public interface Modifiable {
	/**
	 * Rend l'objet non modifiable, non mutable.
	 */
	void makeUnmodifiable();

	/**
	 * @return Si l'objet peut être modifiée.
	 */
	boolean isModifiable();
}
