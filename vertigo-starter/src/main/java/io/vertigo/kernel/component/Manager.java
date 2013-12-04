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
package io.vertigo.kernel.component;

/**
 * Composant.
 * Tout composant doit �tre ThreadSafe. 
 * Un module poss�de un param�trage et un �tat interne,
 * ce qui lui permet d'offrir des services.
 *
 * L'usage du module permet d'enrichir des statistiques.
 *
 * Le module permet de repr�senter
 * - ce qu'il est, ce qu'il fait (ex : module de cache permet de...)
 * - les statistiques d'usage (ex: 99 % d'utilisation du cache)
 * - son �tat interne (ex : 153 Mo utilis� dont 3 Mo sur disque)
 * - son param�trage. (ex : Impl�mentation eh cache avec les param�tres suivants ...)
 *
 * Les statistiques et l'�tat varient au fil du temps.
 * Le param�trage doit �tre stable et n�cessite une reconfiguration. (Nouvelle version d'une application)
 * Le contrat du composant (ce qu'il est, ce qu'il fait) doit �videmment �tre tr�s stable.
 *
 * Lors du d�marrage du composant. (m�thode Start)
 *  - V�rification de la configuration avec les m�thodes register.
 * Lors de l'arr�t du composant 
 *  - Lib�ration des ressources consomm�es par le composant lors du undeploy.
 * 		Exemples : connexions, thread, flux
 * @author pchretien
 */
public interface Manager {
	//Comportements ajoutables : Activeable,  Describable
}
