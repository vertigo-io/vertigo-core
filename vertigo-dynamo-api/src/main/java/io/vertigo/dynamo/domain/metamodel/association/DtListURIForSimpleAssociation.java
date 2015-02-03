package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.dynamo.domain.model.URI;

public final class DtListURIForSimpleAssociation extends DtListURIForAssociation<AssociationSimpleDefinition> {
	private static final long serialVersionUID = -6235569695625996356L;
	private final DefinitionReference<AssociationSimpleDefinition> associationSimpleDefinitionRef;

	public DtListURIForSimpleAssociation(final AssociationSimpleDefinition associationDefinition, final URI source, final String roleName) {
		super(associationDefinition, source, roleName);
		associationSimpleDefinitionRef = new DefinitionReference<>(associationDefinition);
	}

	/**
	 * @return Définition de l'association.
	 */
	public AssociationSimpleDefinition getAssociationDefinition() {
		return associationSimpleDefinitionRef.get();
	}
}
