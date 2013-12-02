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
package io.vertigo.kernel.metamodel;

import java.util.regex.Pattern;

/**
 * D�finition.
 * 
 * Les D�finitions de service, de DT, les domaines, les Formatters sont des d�finitions.
 * De mani�re plus g�n�rale tout �l�ment qui sert � �tablir le mod�le est une d�finition.
 * Une d�finition sert � mod�liser le m�tier.
 *
 * Une d�finition 
 *  - n'est pas serializable.
 *  - est invariante (non mutable) dans le temps.
 *  - est charg�e au (re)d�marrage du serveur.
 *  - poss�de un nom unique qui doit v�rifier le pattern ci dessous
 *
 * @author  pchretien
 * @version $Id: Definition.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public interface Definition {
	char SEPARATOR = '_';
	/**
	 * Expression r�guli�re v�rifi�e par les noms des d�finitions.
	 * 2 exemples accept�s :
	 * TO_TO
	 * ou 
	 * TO_TO$TI_TI
	 */
	Pattern REGEX_DEFINITION_URN = Pattern.compile("[A-Z0-9_]{3,60}([$][A-Z0-9_]{3,60})?");

	/**
	 * @return Nom de la d�finition.
	 */
	String getName();
}
