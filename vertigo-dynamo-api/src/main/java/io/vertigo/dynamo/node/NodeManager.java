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
package io.vertigo.dynamo.node;

import io.vertigo.kernel.component.Manager;

import java.util.List;

/**
 * Gestion des Nodes distribués .
 * Ce manager possède des nodes sous la forme de plugins qui permettent de 
 *  - producer : produire des travaux....
 *  - consumer : consommer des travaux (c'est à dire les réaliser)
 *  - supervisor : vérifier le bon fonctionnement du système
 * 
 * @author npiedeloup, pchretien
 * @version $Id: NodeManager.java,v 1.4 2013/11/15 15:31:33 pchretien Exp $
 */
public interface NodeManager extends Manager {
	/**
	 * @return Liste des noeuds
	 */
	List<Node> getNodes();
}
