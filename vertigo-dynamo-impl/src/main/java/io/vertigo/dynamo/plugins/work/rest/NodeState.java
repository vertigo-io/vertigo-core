package io.vertigo.dynamo.plugins.work.rest;

import io.vertigo.kernel.lang.Assertion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Etat d'un noeud.
 * - id du noeud
 * - liste des types de work pris en charge
 * - date du dernier signe de vie
 * @author npiedeloup
 * @version $Id: NodeState.java,v 1.5 2013/11/15 15:33:20 pchretien Exp $
 */
final class NodeState {

	private final String nodeUID;
	private final Set<String> nodeWorkTypes = Collections.synchronizedSet(new HashSet<String>());
	private long lastSeen;

	/**
	 * Constructeur. 
	 * @param nodeUID id du noeud
	 * @param nodeWorkType type de work pris en charge (cumulatif)
	 */
	public NodeState(final String nodeUID, final String nodeWorkType) {
		Assertion.checkArgNotEmpty(nodeUID);
		Assertion.checkNotNull(nodeWorkType);
		//---------------------------------------------------------------------
		this.nodeUID = nodeUID;
		nodeWorkTypes.add(nodeWorkType);
		lastSeen = System.currentTimeMillis();
	}

	/**
	 * @param nodeWorkType type de work pris en charge (cumulatif)
	 */
	public void touch(final String nodeWorkType) {
		lastSeen = System.currentTimeMillis();
		nodeWorkTypes.add(nodeWorkType);
	}

	/**
	 * @return Date du dernier signe de vie
	 */
	public long getLastSeenTime() {
		return lastSeen;
	}

	/**
	 * @return Id du noeud
	 */
	public String getNodeUID() {
		return nodeUID;
	}
}
