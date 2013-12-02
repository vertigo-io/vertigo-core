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
package vertigo.trash.resource;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Selecteur de classes. 
 * 
 * @author pchretien
 * @version $Id: ClassSelector.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public interface ClassSelector {
	/**
	 * R�cup�re la liste des classes annot�es.
	 */
	Set<Class<?>> getTypesAnnotatedWith(final Annotation annotation);

	/**
	 * R�cup�re la liste des classes annot�es.
	 */
	Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation);

	/**
	 * R�cup�re la liste des Classes h�ritant d'une classe.
	 * <p/>depends on SubTypesScanner configured, otherwise an empty set is returned
	 */
	<T> Set<Class<? extends T>> getSubTypesOf(final Class<T> type);
}
