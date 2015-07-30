package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.lang.Assertion;

import java.io.Serializable;

/**
 * Classe interne décrivant les champs d'une définition.
 * Permet de serialiser une DT qui par nature n'est pas sérialisable.
 * @author pchretien
 */
final class SerializableDtField implements Serializable {
	private static final long serialVersionUID = 7086269816597674149L;
	private final String name;
	private final String label;
	private final DataType dataType;

	/**
	 * @param fieldName Field name
	 * @param fieldLabel Field label
	 * @param dataType Datatype
	 */
	SerializableDtField(final String fieldName, final String fieldLabel, final DataType dataType) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(fieldLabel);
		Assertion.checkNotNull(dataType);
		//-----
		name = fieldName;
		label = fieldLabel;
		this.dataType = dataType;
	}

	/**
	 * @return Name
	 */
	String getName() {
		return name;
	}

	/**
	 *  @return Label
	 */
	String getLabel() {
		return label;
	}

	/**
	 * @return Datatype
	 */
	DataType getDataType() {
		return dataType;
	}
}
