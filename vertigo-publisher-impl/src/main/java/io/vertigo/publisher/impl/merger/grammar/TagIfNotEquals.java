package io.vertigo.publisher.impl.merger.grammar;

/**
 * @author pchretien, npiedeloup
 * @version $Id: TagIfNotEquals.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
//public car instanci� dynamiquement
public final class TagIfNotEquals extends AbstractTagIf {
	/**
	 * Constructeur.
	 */
	public TagIfNotEquals() {
		super(false, true);
	}
}
