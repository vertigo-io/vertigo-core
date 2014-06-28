package io.vertigo.quarto.publisher.metamodel;

import io.vertigo.kernel.lang.Assertion;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * D�finition d'un noeud dans une structure PublisherDataDefinition.
 * Un noeud contient des champs.
 * Les champs peuvent �tre :
 * - soit simples (valu�s) et de type Boolean, String ou Image 
 * - soit un autre noeud 
 * - soit une liste de noeuds
 *
 * @author npiedeloup, pchretien
 * @version $Id: PublisherNodeDefinition.java,v 1.5 2014/02/27 10:32:26 pchretien Exp $
 */
public final class PublisherNodeDefinition {
	private final Map<String, PublisherField> publisherFieldMap;

	PublisherNodeDefinition(final List<PublisherField> publisherFields) {
		Assertion.checkNotNull(publisherFields);
		//---------------------------------------------------------------------
		publisherFieldMap = new LinkedHashMap<>();
		for (final PublisherField publisherField : publisherFields) {
			registerField(publisherField);
		}
	}

	private void registerField(final PublisherField publisherField) {
		Assertion.checkArgument(!publisherFieldMap.containsKey(publisherField.getName()), "Le champ {0} est d�j� d�clar�.", publisherField.getName());
		//---------------------------------------------------------------------
		publisherFieldMap.put(publisherField.getName(), publisherField);
	}

	/**
	 * Retourne le champ correspondant SOUS CONDITION qu'il existe sinon assertion.
	 *
	 * @param fieldName Nom du champ
	 * @return Champ correspondant
	 */
	public PublisherField getField(final String fieldName) {
		final PublisherField field = publisherFieldMap.get(fieldName);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(field, "Le champ {0} n''est pas dans la d�finition de ce noeud, champs disponibles [{1}]", fieldName, publisherFieldMap.keySet());
		return field;
	}

	/**
	 * @return Collection des champs.
	 */
	public Collection<PublisherField> getFields() {
		return publisherFieldMap.values();
	}
}
