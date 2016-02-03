package io.vertigo.vega.engines.webservice.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ParameterizedType use for JsonDeserializer.
 * @author npiedeloup
 */
final class KnownParameterizedType implements ParameterizedType {
	private final Class<?> rawClass;
	private final Type[] typeArguments;

	KnownParameterizedType(final Class<?> rawClass, final Type paramType) {
		this(rawClass, new Type[] { paramType });
	}

	KnownParameterizedType(final Class<?> rawClass, final Type[] typeArguments) {
		this.rawClass = rawClass;
		this.typeArguments = typeArguments;
	}

	/** {@inheritDoc} */
	@Override
	public Type[] getActualTypeArguments() {
		return typeArguments;
	}

	/** {@inheritDoc} */
	@Override
	public Type getOwnerType() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Type getRawType() {
		return rawClass;
	}
}
