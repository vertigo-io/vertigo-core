package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Dynamic;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.database.statementhandler.DynamicResultMetaData.SerializableDtDefinition;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO dynamique.
 * Fabrication dynamique d'un DTO par sa définition.
 *
 * @author  pchretien
 * @version $Id: DynamicDtObject.java,v 1.4 2014/01/20 17:46:01 pchretien Exp $
 */
final class DynamicDtObject implements DtObject, Dynamic {
	private static final long serialVersionUID = 1L;

	/** Définition Sérializable de l'objet. */
	private final SerializableDtDefinition serializableDefinition;
	private final Map<String, Object> values = new HashMap<>();

	/**
	 * Constructeur
	 * @param serializableDefinition DT serializable
	 */
	DynamicDtObject(final SerializableDtDefinition serializableDefinition) {
		Assertion.checkNotNull(serializableDefinition);
		// ----------------------------------------------------------------------
		this.serializableDefinition = serializableDefinition;
	}

	/** {@inheritDoc} */
	public DtDefinition getDefinition() {
		return serializableDefinition.getDtDefinition();
	}

	/** {@inheritDoc} */
	public void setValue(final DtField dtField, final Object value) {
		values.put(dtField.getName(), value);
	}

	/** {@inheritDoc} */
	public Object getValue(final DtField dtField) {
		return values.get(dtField.getName());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
