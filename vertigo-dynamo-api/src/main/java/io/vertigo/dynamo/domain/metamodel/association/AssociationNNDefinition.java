package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.kernel.lang.Assertion;

/**
 * Définition d'une association NN.
 * @author  jcassignol, pchretien
 * @version $Id: AssociationNNDefinition.java,v 1.3 2013/10/22 12:31:50 pchretien Exp $
 */
public final class AssociationNNDefinition extends AssociationDefinition {
	private final String tableName;

	/**
	 * Constructeur d'une association n-n.
	 * @param tableName Nom de la table
	 * @param associationNodeA Noeud A
	 * @param associationNodeB Noeud B
	 */
	public AssociationNNDefinition(final String name, final String tableName, final AssociationNode associationNodeA, final AssociationNode associationNodeB) {
		super(name, associationNodeA, associationNodeB);
		//----------------------------------------------------------------------
		Assertion.checkNotNull(tableName);
		this.tableName = tableName;
	}

	//==========================================================================
	/**
	 * @return Nom de la table porteuse de la relation NN
	 */
	public String getTableName() {
		return tableName;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssociationSimpleDefinition() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public AssociationSimpleDefinition castAsAssociationSimpleDefinition() {
		throw new IllegalAccessError("Il ne s'agit pas d'une relation simple");
	}

	/** {@inheritDoc} */
	@Override
	public AssociationNNDefinition castAsAssociationNNDefinition() {
		return this;
	}
}
