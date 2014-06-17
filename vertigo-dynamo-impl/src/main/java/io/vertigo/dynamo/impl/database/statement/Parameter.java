package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.domain.metamodel.DataType;

/**
 * Représentation objet d'un paramètre d'un statement.
 * @author pchretien
 */
final class Parameter {
	private final DataType dataType;
	private final boolean in;
	private final boolean out;
	private Object value;

	/**
	 * Constructeur.
	 * @param dataType KDataType
	 * @param in boolean
	 * @param out boolean
	 */
	Parameter(final DataType dataType, final boolean in, final boolean out) {
		this.dataType = dataType;
		this.in = in;
		this.out = out;
	}

	/**
	 * Sauvegarde des valeurs des paramètres de la Requêtes
	 * @param value Valeur du paramètre
	 */
	void setValue(final Object value) {
		this.value = value;
	}

	/**
	 * @return Si paramètre IN
	 */
	boolean isIn() {
		return in;
	}

	/**
	 * @return Si paramètre OUT
	 */
	boolean isOut() {
		return out;
	}

	/**
	 * @return Type du paramètre
	 */
	DataType getDataType() {
		return dataType;
	}

	/**
	 * @return Valeur du paramètre
	 */
	Object getValue() {
		return value;
	}
}
