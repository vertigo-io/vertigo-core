package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.kernel.lang.Assertion;

/**
 * Objet utilisé par FreeMarker.
 * 
 * @author pchretien
 * @version $Id: TemplateAssociation.java,v 1.4 2014/01/20 17:47:58 pchretien Exp $
 */
public final class TemplateAssociation {
	private final AssociationNode associationNode;

	/**
	 * Constructeur.
	 * @param associationNode Noeud de l'association à générer
	 */
	TemplateAssociation(final AssociationNode associationNode) {
		Assertion.checkNotNull(associationNode);
		//-----------------------------------------------------------------
		this.associationNode = associationNode;
	}

	/**
	 * @return Noeud d'une association.
	 */
	public AssociationNode getAssociationNode() {
		return associationNode;
	}

	/**
	 * @return Label du noeud
	 */
	public String getLabel() {
		return associationNode.getLabel();
	}

	/**
	 * @return Role du noeud
	 */
	public String getRole() {
		return associationNode.getRole();
	}

	/**
	 * @return Si la cardinalité max du noeud est multiple
	 */
	public boolean isMultiple() {
		return associationNode.isMultiple();
	}

	/**
	 * @return Si le noeud est navigable
	 */
	public boolean isNavigable() {
		return associationNode.isNavigable();
	}

	/**
	 * @return Type à retourner
	 */
	public String getReturnType() {
		return associationNode.getDtDefinition().getClassCanonicalName();
	}

	/**
	 * @return Urn de la définition de l'association
	 */
	public String getUrn() {
		return associationNode.getAssociationDefinition().getName();
	}
}
