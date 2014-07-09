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
package io.vertigo.dynamo.plugins.kvdatastore.berkeley;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.util.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * Classe qui pour un DtObject permet de lire/écrire un tuple.
 * Le binding est indépendant de la DtDefinition.
 *
 * @author pchretien
 */
final class DataBinding extends TupleBinding {
	/** {@inheritDoc} */
	@Override
	public Object entryToObject(final TupleInput ti) {
		try {
			return doEntryToObject(ti);
		} catch (final Exception e) {
			throw new VRuntimeException(e);
		}
	}

	private static Object doEntryToObject(final TupleInput ti) throws Exception {
		final String className = ti.readString();
		final Object bean = ClassUtil.newInstance(className);

		while (ti.available() > 0) {
			final String fieldName = ti.readString();
			final String type = ti.readString();

			final Object value;
			if ("NULL".equals(type)) {
				value = null;
			} else if ("F".equals(type)) {
				value = ti.readFloat();
			} else if ("L".equals(type)) {
				value = ti.readLong();
			} else if ("I".equals(type)) {
				value = ti.readInt();
			} else if ("D".equals(type)) {
				value = ti.readDouble();
			} else if ("S".equals(type)) {
				value = ti.readString();
			} else {
				throw new IllegalArgumentException(" type " + type + " non reconnu");
			}
			final Field field = bean.getClass().getDeclaredField(fieldName);
			ClassUtil.set(bean, field, value);
		}
		//4. L'objet est fabriqué, rempli. On le retourne.
		return bean;
	}

	/** {@inheritDoc} */
	@Override
	public void objectToEntry(final Object object, final TupleOutput to) {
		try {
			doObjectToEntry(object, to);
		} catch (final Exception e) {
			throw new VRuntimeException(e);
		}
	}

	private static void doObjectToEntry(final Object object, final TupleOutput to) {
		to.writeString(object.getClass().getCanonicalName());
		for (final Field field : object.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			//	System.out.println("field["+field.getName()+"] "+field.getModifiers());
			final Object value = ClassUtil.get(object, field);
			to.writeString(field.getName());
			if (value == null) {
				to.writeString("NULL");
			} else if (value instanceof Float) {
				to.writeString("F");
				to.writeFloat(Float.class.cast(value));
			} else if (value instanceof Long) {
				to.writeString("L");
				to.writeLong(Long.class.cast(value));
			} else if (value instanceof Integer) {
				to.writeString("I");
				to.writeInt(Integer.class.cast(value));
			} else if (value instanceof Double) {
				to.writeString("D");
				to.writeDouble(Double.class.cast(value));
			} else if (value instanceof String) {
				to.writeString("S");
				to.writeString(String.class.cast(value));
			} else {
				throw new IllegalArgumentException(" type " + value.getClass() + " non reconnu");
			}
		}
	}
}
