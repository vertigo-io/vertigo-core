package io.vertigo.dynamo.impl.environment.kernel.meta;

import io.vertigo.kernel.lang.Assertion;

/**
 * @author  pchretien
 */
public enum PrimitiveType {
	/** Integer. */
	Integer(Integer.class),
	/** Double. */
	Double(Double.class),
	/** Boolean. */
	Boolean(Boolean.class),
	/** String. */
	String(String.class);

	/**
	 * Classe java que le Type encapsule.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructeur.
	 *
	 * @param javaClass Classe java encapsulée
	 * @param primitive Si il s'agit d'un type primitif (sinon composite)
	 */
	private PrimitiveType(final Class<?> javaClass) {
		Assertion.checkNotNull(javaClass);
		//----------------------------------------------------------------------
		this.javaClass = javaClass;
	}

	/** {@inheritDoc} */
	public void checkValue(final Object value) {
		//Il suffit de vérifier que la valeur passée est une instance de la classe java définie pour le type Dynamo.
		//Le test doit être effectué car le cast est non fiable par les generics
		if (value != null && !javaClass.isInstance(value)) {
			throw new ClassCastException("Valeur " + value + " ne correspond pas au type :" + this);
		}
	}

	public Object cast(final String stringValue) {
		final String sValue = stringValue == null ? null : stringValue.trim();
		if (sValue == null || sValue.length() == 0) {
			return null;
		}
		switch (this) {
			case Integer:
				return java.lang.Integer.valueOf(sValue);
			case Double:
				return java.lang.Double.valueOf(sValue);
			case String:
				return sValue;
			case Boolean:
				return java.lang.Boolean.valueOf(sValue);
			default:
				throw new IllegalArgumentException("cast de la propriété '" + javaClass + "' non implémenté");
		}
	}
}
