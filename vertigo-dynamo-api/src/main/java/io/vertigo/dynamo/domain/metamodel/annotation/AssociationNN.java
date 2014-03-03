package io.vertigo.dynamo.domain.metamodel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gestion des associations NN.
 *
 * @author  dchallas
 * @version $Id: AssociationNN.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.METHOD })
public @interface AssociationNN {
	/**
	 * Nom logique de l'association.
	 */
	String name();

	/**
	 * Nom de la table de jointure.
	 */
	String tableName();

	/**
	 * Nom du DT possédant la table A utilisée (n).
	 */
	String dtDefinitionA();

	/**
	 * Nom du DT possédant la table A utilisée (n).
	 */
	String dtDefinitionB();

	/**
	 * Si le noeud représentant la table A est navigable.
	 */
	boolean navigabilityA();

	/**
	 * Si le noeud représentant la table B est navigable.
	 */
	boolean navigabilityB();

	/**
	 * Label du role associé au noeud représentant la table A.
	 */
	String labelA();

	/**
	 * Label du role associé au noeud représentant la table B.
	 */
	String labelB();

	/**
	 * Nom du role associé au noeud représentant la table A.
	 */
	String roleA();

	/**
	 * Nom du role associé au noeud représentant la table B.
	 */
	String roleB();

}
