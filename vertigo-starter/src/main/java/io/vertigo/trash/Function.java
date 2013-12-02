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
package vertigo.kernel.lang;

/**
 * Approche fonctionnelle.
 * On d�finit un objet fonction.
 * @author pchretien
 * @version $Id: Function.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 * @param <IN> Type de param�tre d'entr�e de la fonction
 * @param <OUT> Type de paaram�tre de sortie de la fonction
 */
public interface Function<IN, OUT> {
	/**
	 * @param input Param�tre d'entr�e
	 * @return Calcul de la fonction
	 */
	OUT apply(final IN input);
}
