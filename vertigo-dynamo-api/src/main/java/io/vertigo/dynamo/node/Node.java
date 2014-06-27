package io.vertigo.dynamo.node;

import io.vertigo.kernel.lang.Assertion;

/**
 * Node.
 * Un noeud poss�de 
 *  - un �tat variable (actif/inactif)
 *  - une configuration (uid / ip) 
 * 
 * @author npiedeloup, pchretien
 * @version $Id: Node.java,v 1.5 2013/11/15 15:31:33 pchretien Exp $
 */
public final class Node {
	private final String uid;
	//	private final String ip;
	private final boolean active;

	public Node(final String uid, /*final String ip,*/final boolean active) {
		Assertion.checkArgNotEmpty(uid);
		//	Assertion.notEmpty(ip);
		//---------------------------------------------------------------------
		this.uid = uid;
		//	this.ip = ip;
		this.active = active;
	}

	/**
	 * @return Si le noeud est actif
	 */
	public boolean isActive() {
		return active;
	}

	//
	//	/**
	//	 * @return IP du noeud
	//	 */
	//	public String getIP() {
	//		return ip;
	//	}

	/**
	 * @return ID du noeud
	 */
	public String getUID() {
		return uid;
	}
}
