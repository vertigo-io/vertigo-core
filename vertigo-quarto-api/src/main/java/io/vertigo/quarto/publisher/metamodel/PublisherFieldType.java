package io.vertigo.quarto.publisher.metamodel;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.publisher.model.PublisherNode;

import java.util.List;

/**
 * Type des champs.
 *
 * @author npiedeloup, pchretien
 * @version $Id: PublisherFieldType.java,v 1.4 2014/01/28 18:53:45 pchretien Exp $
 */
public enum PublisherFieldType {
	/**
	 * Champ de type chaine.
	 * Primitif. 
	 */
	String,

	/**
	 * Champ de type boolean.
	 * Primitif.
	 */
	Boolean,

	/**
	 * Champ de type List.
	 */
	List,

	/**
	 * Champ de type Objet.
	 */
	Node,

	/**
	 * Champ de type Image.
	 */
	Image;

	/**
	 * Validation du type d'une valeur. 
	 * @param value Valeur 
	 * @return Si la valeur correspond au type du champ. 
	 */
	public boolean checkValue(final Object value) {
		Assertion.checkNotNull(value, "La valeur du champ est obligatoire.");
		//---------------------------------------------------------------------
		switch (this) {
			case Boolean:
				return value instanceof Boolean;
			case Node:
				return value instanceof PublisherNode;
			case List:
				if (!(value instanceof List<?>)) {
					return false;
				}
				//on teste le contenu de la liste, pas la liste elle m�me
				for (final Object object : (List<?>) value) {
					if (!PublisherFieldType.Node.checkValue(object)) {
						return false;
					}
				}
				return true;
			case Image:
				return value instanceof KFile;
			case String:
				return value instanceof String;
			default:
				throw new UnsupportedOperationException();
		}
	}
}
