package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.lang.reflect.Field;

/**
 * Métadonnée liée à un champ.
 *
 * @author  pchretien
 *
 */
public final class DtProperty {
	/**
	 * Propriété standard : longueur max du champ, valeur Integer.
	 */
	public static final Property<Integer> MAX_LENGTH = new Property<>("maxLength", Integer.class);

	/**
	 * Propriété standard : Type des définitions.
	 */
	public static final Property<String> TYPE = new Property<>("type", String.class);

	/**
	 * Proriété Regex de type String.
	 */
	public static final Property<String> REGEX = new Property<>("pattern", String.class);

	/**
	 * Propriété de contrainte : valeur minimum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date min.
	 */
	public static final Property<Double> MIN_VALUE = new Property<>("minValue", Double.class);

	/**
	 * Propriété de contrainte : valeur maximum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date max.
	 */
	public static final Property<Double> MAX_VALUE = new Property<>("maxValue", Double.class);

	/**
	 * Propriété standard : Unité de la valeur, valeur String.
	 */
	public static final Property<String> UNIT = new Property<>("unit", String.class);

	/**
	 * Propriété standard : Type de l'index. (SOLR par exemple)
	 */
	public static final Property<String> INDEX_TYPE = new Property<>("indexType", String.class);

	public static Property<?> valueOf(final String propertyName) {
		try {
			final Field field = DtProperty.class.getDeclaredField(propertyName);
			final Property<?> property = Property.class.cast(field.get(DtProperty.class));
			Assertion.checkNotNull(property);
			return property;
		} catch (final NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new VRuntimeException("Propriete {0} non trouvee sur DtProperty", e, propertyName);
		}
	}
}
