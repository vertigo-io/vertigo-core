/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang.json;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * A {@link Serializable} implementation of {@link ParameterizedType}.
 *
 * Gson 2.14.0+ introduced {@code GsonTypes$ParameterizedTypeImpl} which is NOT {@link Serializable}.
 * Since {@link ParameterizedType} is only an interface, we provide our own serializable implementation
 * that Gson accepts as a valid {@link Type} for {@code fromJson}/{@code toJson} calls.
 *
 * Usage:
 * <pre>
 *   // Instead of TypeToken.getParameterized(ArrayList.class, MyItem.class).getType()
 *   // which is NOT Serializable:
 *   Type serializable = SerializableParameterizedType.of(ArrayList.class, MyItem.class);
 *   gson.fromJson(json, serializable); // works — it IS a ParameterizedType
 *   // And survives Java serialization (BerkeleyDB, etc.)
 * </pre>
 *
 * @author npiedeloup
 */
public final class SerializableParameterizedType implements ParameterizedType, Serializable {

	private static final long serialVersionUID = 1L;

	private final Class<?> rawType;
	private final Type[] actualTypeArguments;
	private final Type ownerType;

	/**
	 * Creates a serializable parameterized type.
	 *
	 * @param rawType the raw class (e.g. {@code ArrayList.class})
	 * @param actualTypeArguments the type arguments (e.g. {@code FileInfoURI.class})
	 */
	private SerializableParameterizedType(final Class<?> rawType, final Type... actualTypeArguments) {
		if (rawType == null) {
			throw new IllegalArgumentException("rawType must not be null");
		}
		this.rawType = rawType;
		this.actualTypeArguments = actualTypeArguments;
		this.ownerType = null; // top-level parameterized type — no owner
	}

	/**
	 * Creates a serializable parameterized type from raw + args.
	 */
	public static SerializableParameterizedType of(final Class<?> rawType, final Type... typeArguments) {
		return new SerializableParameterizedType(rawType, typeArguments);
	}

	/**
	 * Creates a serializable copy from an existing {@link ParameterizedType}
	 * (e.g. the one returned by Gson {@code TypeToken.getParameterized().getType()}).
	 */
	public static SerializableParameterizedType from(final ParameterizedType source) {
		return new SerializableParameterizedType(
				(Class<?>) source.getRawType(),
				source.getActualTypeArguments());
	}

	/** {@inheritDoc} */
	@Override
	public Type[] getActualTypeArguments() {
		return Arrays.copyOf(actualTypeArguments, actualTypeArguments.length);
	}

	/** {@inheritDoc} */
	@Override
	public Type getRawType() {
		return rawType;
	}

	/** {@inheritDoc} */
	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	@Override
	public String toString() {
		return rawType.getName() + "<" + String.join(", ", Arrays.stream(actualTypeArguments)
				.map(Type::getTypeName)
				.toArray(String[]::new)) + ">";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final SerializableParameterizedType that = (SerializableParameterizedType) o;
		return rawType.equals(that.rawType)
				&& Arrays.equals(actualTypeArguments, that.actualTypeArguments);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		int result = rawType.hashCode();
		result = 31 * result + Arrays.hashCode(actualTypeArguments);
		return result;
	}
}
