package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Dynamic;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO dynamique.
 * Fabrication dynamique d'un DTO par sa définition.
 *
 * @author  pchretien
 */
public final class DynaDtObject implements DtObject, Dynamic {
	private static final long serialVersionUID = 1L;

	/** Définition Sérializable de l'objet. */
	private final DefinitionReference<DtDefinition> dtDefinitionRef;
	private final Map<String, Object> values = new HashMap<>();

	/**
	 * Constructeur
	 * @param dtDefinition DT
	 */
	public DynaDtObject(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		dtDefinitionRef = new DefinitionReference<>(dtDefinition);
	}

	/** {@inheritDoc} */
	public DtDefinition getDefinition() {
		return dtDefinitionRef.get();
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
