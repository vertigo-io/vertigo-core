package io.vertigo.dynamo.domain.metamodel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gestion des associations.
 *
 * @author  pchretien, evernat
 * @version $Id: Association.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.METHOD })
public @interface Association {
	/**
	 * Nom logique de l'association.
	 */
	String name();

	/**
	 * Nom du champ représentant la FK.
	 */
	String fkFieldName();

	/**
	 * Nom du DT possédant la clé primaire utilisée (n).
	 */
	String primaryDtDefinitionName();

	/**
	 * Si le noeud représentant la clé primaire est navigable.
	 */
	boolean primaryIsNavigable();

	/**
	 * Nom du role associé au noeud représentant la clé primaire.
	 */
	String primaryRole();

	/**
	 * Label du role associé au noeud représentant la clé primaire.
	 */
	String primaryLabel();

	/**
	 * Multiplicité du noeud représentant la clé primaire.
	 */
	String primaryMultiplicity();

	//--------------------------------------------------------------------------
	/**
	 * Nom du DT possédant la clé étrangère utilisée (1).
	 */
	String foreignDtDefinitionName();

	/**
	 *  Si le noeud représentant la clé étrangère est navigable.
	 */
	boolean foreignIsNavigable();

	/**
	 * Nom du role associé au noeud représentant la clé étrangère.
	 */
	String foreignRole();

	/**
	 * Label du role associé au noeud représentant la clé étrangère.
	 */
	String foreignLabel();

	/**
	 * Multiplicité du noeud représentant la clé étrangère.
	 */
	String foreignMultiplicity();
}
