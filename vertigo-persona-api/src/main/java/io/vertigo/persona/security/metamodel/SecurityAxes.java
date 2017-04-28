/**
 *
 */
package io.vertigo.persona.security.metamodel;

import java.util.List;

import io.vertigo.dynamo.domain.metamodel.DtField;

/**
 * Axe de sécurité.
 *
 * @author jgarnier
 */
public class SecurityAxes {

	private final SecurityAxeType type;
	private final DtField field;
	private final List<String> values;

	/**
	 * Construct an instance of Axe.
	 *
	 * @param type type.
	 * @param field champ.
	 * @param values valeurs possible (pour le type ENUM).
	 */
	public SecurityAxes(final SecurityAxeType type, final DtField field, final List<String> values) {
		this.type = type;
		this.field = field;
		this.values = values;
	}

	/**
	 * Give the value of type.
	 *
	 * @return the value of type.
	 */
	public SecurityAxeType getType() {
		return type;
	}

	/**
	 * Give the value of field.
	 *
	 * @return the value of field.
	 */
	public DtField getField() {
		return field;
	}

	/**
	 * Give the value of values.
	 *
	 * @return the value of values.
	 */
	public List<String> getValues() {
		return values;
	}
}
