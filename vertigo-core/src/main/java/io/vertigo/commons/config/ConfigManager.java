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
package io.vertigo.commons.config;

import io.vertigo.lang.Component;

import java.util.regex.Pattern;

/**
 * Interface du gestionnaire de la configuration applicative.
 * Une configuration est identifiée par un chemin et possède une liste de propriétés.
 * Une propriété est identifiée par un nom et possède une valeur.
 * Le chemmin est camelCase.camelCase et ne contient que des lettres et chiffres; les séparateurs sont des points.
 * 	- Une propriété est camelCase et ne contient que des lettres et chiffres.
 *  - Une regex précise les chaines autorisées.
 *
 * Les propriétés sont de trois types :
 * -boolean
 * -String
 * -int
 *
 * Le chemin des configuration est hiérachique, il y a un héritage implicite des propriétés.
 * Le séparateur est le caractère point (.)
 *
 * Même si une configuration n'est pas déclarée, la remontée est automatique.
 *
 *
 * Exemple :
 *
 * maconf:{
 *  mapropriete1:toto,
 *  mapropriete2:titi
 * }
 *
 * maconf.subConf1:{
 *  mapropriete2:tata,
 *  mapropriete3:titi
 * }
 *
 * maconf.subConf2:{
 *  mapropriete3:tata
 * }
 *
 * getStringValue(maconf, mapropriete1) => toto
 * getStringValue(maconf.subConf1, mapropriete1) => toto  #La propriété 'mapropriete1' n'étant pas trouvée on remonte au parent.
 *
 * getStringValue(maconf, mapropriete2) => titi
 * getStringValue(maconf.subConf1, mapropriete2) => tata #La propriété 'mapropriete2' est surchargée
 *
 * getStringValue(maconf.subConf2, mapropriete3) => tata
 * getStringValue(maconf, mapropriete3) => erreur #'mapropriete3' n'est pas déclarée dans maConf
 *
 * getStringValue(maconf.unknown, mapropriete2) => titi
 * getStringValue(maconf.subConf1.unknown, mapropriete2) => tata
 *
 * @author prahmoune, npiedeloup
 */
public interface ConfigManager extends Component {
	Pattern REGEX_PATH = Pattern.compile("([a-z][a-zA-Z0-9]*)(\\.[a-z][a-zA-Z0-9]*)*");
	Pattern REGEX_PROPERTY = Pattern.compile("[a-z][a-zA-Z0-9]*");

	/**
	 * Retourne une implémentation à partir d'une interface ou d'un Bean.
	 * Celà permet de structurer les développements.
	 * @param <C> Type de l'interface de la configuration
	 * @param configPath Chemin décrivant la configuration
	 * @param configClass Interface ou Class de la configuration
	 */
	<C> C resolve(final String configPath, final Class<C> configClass);

	/**
	 * Retourne une propriété de configuration.
	 * @param configPath Chemin décrivant la configuration
	 * @param property Nom de la propriété de la configuration
	 * @return Valeur de la propriété
	 */
	String getStringValue(String configPath, String property);

	/**
	 * Retourne une propriété de configuration.
	 * @param configPath Chemin décrivant la configuration
	 * @param property Propriété de la configuration
	 * @return Valeur de la propriété
	 */
	int getIntValue(String configPath, String property);

	/**
	 * Retourne une propriété de configuration.
	 * @param configPath Chemin décrivant la configuration
	 * @param property Propriété de la configuration
	 * @return Valeur de la propriété
	 */
	boolean getBooleanValue(String configPath, String property);
}
