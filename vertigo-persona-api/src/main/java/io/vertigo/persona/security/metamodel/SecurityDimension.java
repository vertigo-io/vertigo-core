/**
 *
 */
package io.vertigo.persona.security.metamodel;

import java.util.List;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;

/**
 * Dimension de sécurité.
 *
 * @author jgarnier
 */
public class SecurityDimension {

	private final String name;
	private final SecurityDimensionType type;
	private final List<DtField> fields;
	private final List<String> values;

	/**
	 * Construct an instance of SecurityDimension.
	 *
	 * @param name name.
	 * @param type type.
	 * @param fields Ordered list of fields (multiple for TREE, empty for ENUM).
	 * @param values Ordered list of values (empty for TREE, multiple for ENUM).
	 */
	public SecurityDimension(final String name, final SecurityDimensionType type, final List<DtField> fields, final List<String> values) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(fields);
		Assertion.checkNotNull(values);
		Assertion.when(SecurityDimensionType.ENUM.equals(type)).check(() -> fields.isEmpty() && values.size() > 1, "SecurityDimension of type ENUM ({0}) needs the ordered list of values and no field (name is use)", name);
		Assertion.when(SecurityDimensionType.TREE.equals(type)).check(() -> fields.size() > 1 && values.isEmpty(), "SecurityDimension of type TREE ({0}) needs more than on fields and the no values", name);
		//----
		this.name = name;
		this.type = type;
		this.fields = fields;
		this.values = values;
	}

	/**
	 * Give the name of this dimension.
	 *
	 * @return the name of this dimension.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Give the value of type.
	 *
	 * @return the value of type.
	 */
	public SecurityDimensionType getType() {
		return type;
	}

	/**
	 * Give the ordered list of fields (multiple for TREE, empty for ENUM)
	 *
	 * @return the ordered list of fields.
	 */
	public List<DtField> getFields() {
		return fields;
	}

	/**
	 * Give the ordered list of values (empty for TREE, multiple for ENUM).
	 *
	 * @return the  ordered list of values.
	 */
	public List<String> getValues() {
		return values;
	}
}
