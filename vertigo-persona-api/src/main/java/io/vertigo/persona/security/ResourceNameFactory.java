package io.vertigo.persona.security;

/**
 * Construit la chaine de resource pour un objet donn�.
 * @author npiedeloup
 * @version $Id: ResourceNameFactory.java,v 1.1 2013/09/23 13:52:37 npiedeloup Exp $
 */
public interface ResourceNameFactory {

	/**
	 * @param value Objet s�curis�
	 * @return Chaine de s�curit� de la resource.
	 */
	String toResourceName(Object value);
}
