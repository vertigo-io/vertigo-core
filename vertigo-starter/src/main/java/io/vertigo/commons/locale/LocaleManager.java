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
package io.vertigo.commons.locale;

import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.MessageKey;
import io.vertigo.kernel.lang.MessageText;

import java.util.Locale;


/**
 * Toute application g�r�e par kapser est multilingue ou plus pr�cis�mment multidictionnaires.
 * 
 * Il est possible de g�rer des ressources externalis�es dans des dictionnaires.
 * 
 * Toute ressource est identifi�e par une cl� :  @see MessageKey
 * Pour un composant donn�, la liste des cl�s est impl�ment�e id�alement sous la forme d'une enum.
 * Un fichier de ressource, appel� dictionnaire est associ�e � la liste des cl�s.
 * 
 * Si le libell� n'est pas trouv� dans une langue, on renvoie un message "panic", en pr�cisant la langue demand�e 
 * de plus on loggue un warning.
 * 
 * Exemple message panic :
 * MessageText(null,messageKey.TOTO) en 'fr_FR' : <<fr:TOTO>>
 * MessageText(null,messageKey.TOTO) en 'en' : <<en:TOTO>>
 * 
 * Un libell� peut �tre param�tr�.
 * 
 * @see MessageText permet de cr�er des libell�s connect� au dictionnaire.
 * 
 *  
 * @author pchretien
 * @version $Id: LocaleManager.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public interface LocaleManager extends Manager {
	//=========================================================================
	//======================M�thodes d'initialisation =========================
	//=========================================================================
	/**
	 * Enregistre une strat�gie de choix de langue.
	 * @param localeProvider D�finit la langue par d�faut de fa�on contextuelle
	 */
	void registerLocaleProvider(final LocaleProvider localeProvider);

	/**
	 * Ajout d'un dictionnaire de ressources.
	 * Toutes les ressources identifi�es par une cl� doivent �tre pr�sente dans le fichier properties.
	 * Cette m�thode est non synchronis�e etdoit �tre appel�e lors du d�marrage de l'application.
	 * @param baseName Nom et chemin du fichier properties
	 * @param enums Enum�ration (enum) de contr�le des ressources g�r�ees
	 */
	void add(final String baseName, final MessageKey[] enums);

	/**
	 * Surcharge d'un dictionnaire de ressources.
	 * Cette m�thode est non synchronis�e et doit �tre appel�e lors du d�marrage de l'application.
	 * Il est possible de ne surcharger qu'une propri�t�.
	 * Il est possible de ne renseigner qu'un des dictionnaire (et donc de ne pas renseigner tous les bundles).
	 * @param baseName Nom et chemin du fichier properties
	 * @param enums Enum�ration (enum) de contr�le des ressources g�r�ees
	 */
	void override(final String baseName, final MessageKey[] enums);

	//=========================================================================
	//=========================================================================
	//=========================================================================
	/**
	 * Retourne la locale courante.
	 * C'est � dire correspondant � l'utilisateur courant si il y en a un,
	 * sinon correspond � la locale de l'application.
	 * @return Locale courante
	 */
	Locale getCurrentLocale();

	/**
	 * Retourne le libell� non formatt� d'un message identifi� par sa cl�.
	 * Retourne null si le message n'est pas trouv�
	 *
	 * @param messageKey cl� du message .
	 * @param locale Locale 
	 * @return Message non formatt� dans la langue de la locale.
	  */
	String getMessage(final MessageKey messageKey, final Locale locale);

}
