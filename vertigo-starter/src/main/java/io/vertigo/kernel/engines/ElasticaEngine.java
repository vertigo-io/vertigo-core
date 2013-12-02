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
package io.vertigo.kernel.engines;

import io.vertigo.kernel.Engine;

/** 
 * Gestion des appels distribu�s.
 * Ce module est utilis� 
 *  - soit en mode client 
 *  - soit en mode server
 *  
 * @author pchretien
 * @version $Id: ElasticaEngine.java,v 1.1 2013/11/18 15:12:17 pchretien Exp $
 */
public interface ElasticaEngine extends Engine {
	/**
	 * Cr�ation d'un proxy client. 
	 * Le proxy permet de distribuer des services ; ces services sont d�clar�s dans une interface et d�ploy�s sur un serveur.  
	 * 
	 * @param <F> Type de l'interface � distribuer
	 * @return Interface cliente du service
	 */
	<F> F createProxy(final Class<F> facadeClass);

}
