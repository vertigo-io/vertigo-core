package io.vertigo.security;

/**
 * Construit la chaine de resource pour un objet donné.
 * @author npiedeloup
 */
public interface ResourceNameFactory {

	/**
	 * @param value Objet sécurisé
	 * @return Chaine de sécurité de la resource.
	 */
	String toResourceName(Object value);
}
