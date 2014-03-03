package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;

/**
 * URI d'une liste définie par une association.
 *
 * @author pchretien
 * @version $Id: DtListURIForAssociation.java,v 1.5 2014/01/08 15:19:56 pchretien Exp $
 */
public final class DtListURIForAssociation extends DtListURI {
	private static final long serialVersionUID = 5933412183954919000L;

	private final String associationDefinitionName;
	private final String roleName;
	private final URI<? extends DtObject> source;

	/**
	 * Constructeur.
	 * @param associationDefinition Définition de l'association
	 * @param source URI (Clé primaire) du dtObject source
	 * @param roleName Nom du rôle
	 */
	public DtListURIForAssociation(final AssociationDefinition associationDefinition, final URI<? extends DtObject> source, final String roleName) {
		super(AssociationUtil.getAssociationNode(associationDefinition, roleName).getDtDefinition());
		Assertion.checkNotNull(associationDefinition);
		Assertion.checkNotNull(source);
		Assertion.checkNotNull(roleName);
		//----------------------------------------------------------------------
		associationDefinitionName = associationDefinition.getName();
		this.roleName = roleName;

		/**
		 * Noeud correspondant au role
		 */
		final AssociationNode target = AssociationUtil.getAssociationNode(associationDefinition, roleName);

		//On vérifie la cardinalité de la cible
		Assertion.checkArgument(target.isMultiple(), "le noeud cible doit être multiple");

		this.source = source;
	}

	/**
	 * @return Clé identifiant la ressource parmi les ressources du même type.
	 * Exemple :
	 */
	public URI<? extends DtObject> getSource() {
		return source;
	}

	/**
	 * @return String Nom du rôle représentant la collection dans l'association
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * @return Définition de l'association.
	 */
	public AssociationDefinition getAssociationDefinition() {
		return Home.getDefinitionSpace().resolve(associationDefinitionName, AssociationDefinition.class);
	}
}
