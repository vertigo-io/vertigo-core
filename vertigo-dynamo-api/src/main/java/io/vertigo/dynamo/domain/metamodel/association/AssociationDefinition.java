package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Décrit une association entre deux objets (A et B)
 * L'association permet de décrire d'un point de vue conceptuel :
 * - les cardinalités, (notnull ; multiplicity )
 * - les roles,
 * - la navigation,
 * - les deux types d'objets ou DtDefinition ou classes mises en oeuvre.
 *
 * L'association permet aussi de décrire les choix d'implémentation effectués
 * - Foreign key
 * - Nom de table
 *
 *
 * 3 cas de figure :
 *  >>Relation simple     A-B = (0)1 -- (0)1
 *  >>Relation multpliple A-B = (0)1 -- *
 *  >>Relation complexe   A-B =   *  -- *
 *  Pour la relation simple on copie la clé de B dans A
 *  Pour la relation multiple on copie la relation de A dans B
 *  Pour la relation complexe XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author  jcassignol, pchretien
 */
@Prefix("A")
public abstract class AssociationDefinition implements Definition {
	/**
	 * Nom de la définition.
	 */
	private final String name;
	private final AssociationNode associationNodeA;
	private final AssociationNode associationNodeB;

	/**
	 * Constructeur.
	 * @param associationNodeA Noeud A
	 * @param associationNodeB Noeud B
	 */
	AssociationDefinition(final String name, final AssociationNode associationNodeA, final AssociationNode associationNodeB) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(associationNodeA);
		Assertion.checkNotNull(associationNodeB);
		//----------------------------------------------------------------------
		this.name = name;
		this.associationNodeA = associationNodeA;
		this.associationNodeB = associationNodeB;
		associationNodeA.setAssociationDefinition(this);
		associationNodeB.setAssociationDefinition(this);
	}

	/**
	 * @return Si il s'agit d'une association simple
	 */
	public abstract boolean isAssociationSimpleDefinition();

	/**
	 * @return Association castée en simple
	 */
	public abstract AssociationSimpleDefinition castAsAssociationSimpleDefinition();

	/**
	 * @return Association castée en NN
	 */
	public abstract AssociationNNDefinition castAsAssociationNNDefinition();

	/**
	 * Noeud A de l'association.
	 * @return AssociationNode
	 */
	public final AssociationNode getAssociationNodeA() {
		return associationNodeA;
	}

	/**
	 * Noeud B de l'association.
	 * @return AssociationNode
	 */
	public final AssociationNode getAssociationNodeB() {
		return associationNodeB;
	}

	/** {@inheritDoc} */
	public final String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}
}
