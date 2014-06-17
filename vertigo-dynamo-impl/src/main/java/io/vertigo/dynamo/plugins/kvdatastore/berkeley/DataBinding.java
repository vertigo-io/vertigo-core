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

	private Object doEntryToObject(final TupleInput ti) throws Exception {
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

	private void doObjectToEntry(final Object object, final TupleOutput to) {
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
