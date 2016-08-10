/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.environment.java.data;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.dynamo.environment.java.data.domain.Attachment;
import io.vertigo.dynamo.environment.java.data.domain.City;
import io.vertigo.dynamo.environment.java.data.domain.Command;
import io.vertigo.dynamo.environment.java.data.domain.CommandCriteria;
import io.vertigo.dynamo.environment.java.data.domain.CommandType;
import io.vertigo.dynamo.environment.java.data.domain.CommandValidation;

/**
 * Attention cette classe est générée automatiquement !
 */
public final class DtDefinitions implements Iterable<Class<?>> {

	/**
	 * Enumération des DtDefinitions.
	 */
	public enum Definitions {
		/** Objet de données Attachment. */
		Attachment(Attachment.class),
		/** Objet de données City. */
		City(City.class),
		/** Objet de données Command. */
		Command(Command.class),
		/** Objet de données CommandCriteria. */
		CommandCriteria(CommandCriteria.class),
		/** Objet de données CommandType. */
		CommandType(CommandType.class),
		/** Objet de données CommandValidation. */
		CommandValidation(CommandValidation.class),;

		private final Class<?> clazz;

		private Definitions(final Class<?> clazz) {
			this.clazz = clazz;
		}

		/**
		  * Classe associée.
		  * @return Class d'implémentation de l'objet
		  */
		public Class<?> getDtClass() {
			return clazz;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Class<?>> iterator() {
		return new Iterator<Class<?>>() {
			private final Iterator<Definitions> it = Arrays.asList(Definitions.values()).iterator();

			/** {@inheritDoc} */
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			/** {@inheritDoc} */
			@Override
			public Class<?> next() {
				return it.next().getDtClass();
			}

			/** {@inheritDoc} */
			@Override
			public void remove() {
				//unsupported
			}
		};
	}
}
