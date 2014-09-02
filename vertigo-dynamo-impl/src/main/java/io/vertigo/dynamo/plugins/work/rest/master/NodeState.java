/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.work.rest.master;

import io.vertigo.core.lang.Assertion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Etat d'un noeud.
 * - id du noeud
 * - liste des types de work pris en charge
 * - date du dernier signe de vie
 * @author npiedeloup
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
