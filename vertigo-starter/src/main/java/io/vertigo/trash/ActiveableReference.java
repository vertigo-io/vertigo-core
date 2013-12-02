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
* Objet assurant le cycle de vie d'un composant en masquant son impl�mentation (Activeable) par une interface(non Activeable). 
* 
 * @author pchretien
 * @version $Id: ActiveableReference.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public final class ActiveableReference<T> implements Activeable {
	private final Activeable object;
	private final Class<T> clazz;

	public ActiveableReference(final Activeable object, final Class<T> clazz) {
		Assertion.notNull(object);
		Assertion.notNull(clazz);
		//---------------------------------------------------------------------
		this.object = object;
		this.clazz = clazz;
	}

	/** {@inheritDoc} */
	public void start() {
		object.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		object.stop();
	}

	public T get() {
		return clazz.cast(object);
	}
}
