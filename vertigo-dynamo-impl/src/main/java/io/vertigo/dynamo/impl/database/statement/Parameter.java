package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.domain.metamodel.KDataType;

/**
 * Représentation objet d'un paramètre d'un statement.
 * @author pchretien
 * @version $Id: Parameter.java,v 1.2 2014/01/20 17:46:01 pchretien Exp $
 */
final class Parameter {
	private final KDataType dataType;
	private final boolean in;
	private final boolean out;
	private Object value;

	/**
	 * Constructeur.
	 * @param dataType KDataType
	 * @param in boolean
	 * @param out boolean
	 */
	Parameter(final KDataType dataType, final boolean in, final boolean out) {
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
	KDataType getDataType() {
		return dataType;
	}

	/**
	 * @return Valeur du paramètre
	 */
	Object getValue() {
		return value;
	}
}
