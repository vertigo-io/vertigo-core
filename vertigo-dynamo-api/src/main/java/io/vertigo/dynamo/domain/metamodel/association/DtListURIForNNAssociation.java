package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.dynamo.domain.model.URI;

public final class DtListURIForNNAssociation extends DtListURIForAssociation<AssociationNNDefinition> {
	private static final long serialVersionUID = -6235569695625996356L;
	private final DefinitionReference<AssociationNNDefinition> associationNNDefinitionRef;

	public DtListURIForNNAssociation(final AssociationNNDefinition associationDefinition, final URI source, final String roleName) {
		super(associationDefinition, source, roleName);
		associationNNDefinitionRef = new DefinitionReference<>(associationDefinition);
	}

	/**
	 * @return Définition de l'association.
	 */
	public AssociationNNDefinition getAssociationDefinition() {
		return associationNNDefinitionRef.get();
	}

}
