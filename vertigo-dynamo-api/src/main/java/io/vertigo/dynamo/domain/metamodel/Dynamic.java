package io.vertigo.dynamo.domain.metamodel;

/**
 * Permet d'accéder aux données d'un objet de façon dynamique.
 * @author  pchretien
 */
public interface Dynamic {
	/**
	 * @return Définition de la resource.
	 */
	DtDefinition getDefinition();

	/**
	* Setter Générique.
	* Garantit que la valeur passée est conforme
	*  - au type enregistré pour le champ
	*  - les contraintes ne sont pas vérifiées.
	*
	* @param value Object
	*/
	void setValue(final DtField dtField, final Object value);

	/**
	 * Getter générique.
	 * Garantit que la valeur retournée est conforme
	 *  - au type enregistré pour le champ
	 *
	 * @return valeur non typée
	 */
	Object getValue(final DtField dtField);
}
