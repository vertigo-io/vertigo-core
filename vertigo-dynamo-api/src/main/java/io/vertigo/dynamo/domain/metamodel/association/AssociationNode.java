package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.JsonExclude;
import io.vertigo.kernel.metamodel.DefinitionReference;

/**
 * Noeud d'une association.
 *
 * @author  jcassignol, pchretien
 * @version $Id: AssociationNode.java,v 1.7 2013/10/22 12:31:50 pchretien Exp $
 */
public final class AssociationNode {
	@JsonExclude
	//On exclue pour éviter une boucle
	private AssociationDefinition associationDefinition;

	private final DefinitionReference<DtDefinition> dtDefinitionRef;
	private final boolean navigable;
	private final String role;
	private final String label;
	private final boolean multiple;
	private final boolean notNull;

	/**
	 * Constructeur d'un noeud.
	 * @param dtDefinition Définition de DT
	 * @param isNavigable Si le noeud est navigable (i.e. visible)
	 * @param role Role
	 * @param label Label
	 * @param isMultiple Si la cardinalité max est multiple (au plus)
	 * @param isNotNull Si la cardinalité min est non null (au moins)
	 */
	public AssociationNode(final DtDefinition dtDefinition, final boolean isNavigable, final String role, final String label, final boolean isMultiple, final boolean isNotNull) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(label);
		Assertion.checkNotNull(role);
		Assertion.checkArgument(role.indexOf(' ') == -1, "Le role ne doit pas être un label : {0}", role);
		//----------------------------------------------------------------------
		this.dtDefinitionRef = new DefinitionReference<>(dtDefinition);
		this.role = role;
		this.label = label;
		navigable = isNavigable;
		notNull = isNotNull;
		multiple = isMultiple;
	}

	//Mis à jour lors de la création de l'association. 
	void setAssociationDefinition(final AssociationDefinition associationDefinition) {
		Assertion.checkNotNull(associationDefinition);
		Assertion.checkState(this.associationDefinition == null, "variable deja affectee");
		//----------------------------------------------------------------------
		this.associationDefinition = associationDefinition;
	}

	/**
	 * @return Définition de l'association possédant ce noeud
	 */
	public AssociationDefinition getAssociationDefinition() {
		Assertion.checkNotNull(associationDefinition);
		//----------------------------------------------------------------------
		return associationDefinition;
	}

	/**
	 * @return DT (classe de l'objet métier) associé au noeud
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinitionRef.get();
	}

	/**
	 * @return Si le noeud est navigable
	 */
	public boolean isNavigable() {
		return navigable;
	}

	/**
	 * @return Role du noeud
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @return Label du noeud
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Si la cardinalité max du noeud est multiple
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * @return Si la cardinalité min du noeud est égale à 1
	 */
	public boolean isNotNull() {
		return notNull;
	}
	//	/** {@inheritDoc} */
	//	@Override
	//	public String toString() {
	//		return "AssociationNode[" + this.dtDefinition + ", " + this.role + ", " + this.label + ", " + this.navigable + ", " + this.notNull + ", " + this.multiple + "]";
	//	}
}
