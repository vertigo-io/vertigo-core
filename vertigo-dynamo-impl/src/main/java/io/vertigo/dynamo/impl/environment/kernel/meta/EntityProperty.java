package io.vertigo.dynamo.impl.environment.kernel.meta;


/**
 * Propriété (meta-data, aspect, attribute) d'une entity.
 *
 * @author  pchretien, npiedeloup
 * @version $Id: EntityProperty.java,v 1.4 2014/01/20 17:46:11 pchretien Exp $
 */
public interface EntityProperty {
	/**
	 * @return Nom de la propriété (Const)
	 */
	String getName();

	/**
	 * Toute propriété dynamo est déclarée dans un type primitif .
	 * Ceci permet de gérer au mieux l'utilisation des propriétés dans la grammaire.
	 * @return Type primitif utilisé pour déclarer la valuer de la propriété.
	 */
	PrimitiveType getPrimitiveType();
}
