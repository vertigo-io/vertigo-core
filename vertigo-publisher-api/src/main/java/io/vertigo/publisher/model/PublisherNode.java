package io.vertigo.publisher.model;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.publisher.metamodel.PublisherField;
import io.vertigo.publisher.metamodel.PublisherFieldType;
import io.vertigo.publisher.metamodel.PublisherNodeDefinition;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conteneur de donn�es utilis� par Publisher au sein d'un PublisherData.
 *
 * Impl�mentation d'un noeud dans une structure PublisherData.
 * Un noeud contient des champs.
 * Les champs peuvent �tre :
 * - soit simple (valu�s) et de type Boolean, String ou Image 
 * - soit un autre noeud, 
 * - soit une liste de noeud.
 *
 * @author npiedeloup, pchretien
 * @version $Id: PublisherNode.java,v 1.7 2014/02/27 10:32:26 pchretien Exp $
 */
public final class PublisherNode implements Serializable {
	private static final long serialVersionUID = 2174855665059480516L;

	private final PublisherNodeDefinition nodeDefinition;
	private final Map<String, Object> dataMap = new HashMap<>();

	PublisherNode(final PublisherNodeDefinition nodeDefinition) {
		Assertion.checkNotNull(nodeDefinition);
		//---------------------------------------------------------------------
		this.nodeDefinition = nodeDefinition;
		//On initialise toutes les champs de type liste � vide.
		for (final PublisherField field : getNodeDefinition().getFields()) {
			if (field.getFieldType() == PublisherFieldType.List) {
				dataMap.put(field.getName(), Collections.emptyList());
			}
		}
	}

	/**
	 * @return Definition de ce noeud
	 */
	public PublisherNodeDefinition getNodeDefinition() {
		return nodeDefinition;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	private void check(final PublisherFieldType fieldType, final String fieldName, final Object value) {
		Assertion.checkNotNull(fieldName, "Le nom du champ est obligatoire.");
		Assertion.checkNotNull(value, "La valeur du champ {0} est obligatoire.", fieldName);
		//---------------------------------------------------------------------
		final PublisherFieldType currentFieldType = nodeDefinition.getField(fieldName).getFieldType();
		Assertion.checkArgument(currentFieldType == fieldType, "Le field {0} n''est pas du type {1} mais de type {2}", fieldName, fieldType, currentFieldType);
		Assertion.checkArgument(fieldType.checkValue(value), "La valeur {0} n'est pas conforme au type '{1}' sur le champ '{2}'", value, fieldType, fieldName);
	}

	/**
	 * Cr�e un PublisherNodeData pour un champs donn�e.
	 * @param fieldName Nom du champ.
	 * @return PublisherNodeDataWritable pour ce champ.
	 */
	public PublisherNode createNode(final String fieldName) {
		final Option<PublisherNodeDefinition> childNodeDefinition = nodeDefinition.getField(fieldName).getNodeDefinition();
		Assertion.checkArgument(childNodeDefinition.isDefined(), "Le champ {0} n'accepte pas l'ajout de noeud", fieldName);
		return new PublisherNode(childNodeDefinition.get());
	}

	//-------------------------------------------------------------------------
	//------------------------GetValue-----------------------------------------
	//-------------------------------------------------------------------------
	/**
	 * R�cup�re une valeur affichable.
	 * @param fieldName Nom du champ 
	 * @return Chaine � afficher pour le champ
	 */
	public String getString(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.String, String.class);
	}

	/**
	 * R�cup�re une valeur bool�enne.
	 * @param fieldName Nom du champ 
	 * @return Valeur bool�enne du champ
	 */
	public boolean getBoolean(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.Boolean, Boolean.class);
	}

	/**
	 * Permet de r�cup�rer un champs de type objet.
	 * @param fieldName Nom du champ 
	 * @return PublisherDataNode port� par ce champ
	 */
	public PublisherNode getNode(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.Node, PublisherNode.class);
	}

	/**
	 * Permet de r�cup�rer un champs de type image.
	 * @param fieldName Code de l'image
	 * @return Image port�e par ce champ
	 */
	public KFile getImage(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.Image, KFile.class);
	}

	/**
	 * R�cup�re la liste des noeuds d'un champlistes.
	 * @param fieldName Nom du champ 
	 * @return Liste de PublisherDataNode port�e par ce champ
	 */
	public List<PublisherNode> getNodes(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.List, List.class);
	}

	private <C> C getValue(final String fieldName, final PublisherFieldType fieldType, final Class<C> clazz) {
		final int index = fieldName.indexOf('.');
		if (index != -1) {
			return getValueByPath(index, fieldName, fieldType, clazz);
		}
		final Object value = dataMap.get(fieldName);
		check(fieldType, fieldName, value);
		//---------------------------------------------------------------------
		return clazz.cast(value);
	}

	private <C> C getValueByPath(final int pathIndex, final String fieldPath, final PublisherFieldType fieldType, final Class<C> clazz) {
		final String nodeName = fieldPath.substring(0, pathIndex);
		final PublisherNode node;
		try {
			node = getNode(nodeName);
		} catch (final NullPointerException e) {//on catch juste pour reformuler l'erreur
			if (e.getMessage().contains("est obligatoire")) {
				Assertion.checkNotNull(null, "L'objet {0} utilis� pour le champs {1} est null. Dans votre mod�le vous devez tester l'existance de cet objet.", nodeName, fieldPath);
			}
			throw e;
		}
		return node.getValue(fieldPath.substring(pathIndex + 1), fieldType, clazz);
	}

	//-------------------------------------------------------------------------
	//------------------------SetValue-----------------------------------------
	//-------------------------------------------------------------------------
	/**
	 * Fixe une valeur affichable.
	 * @param fieldName Nom du champ 
	 * @param value Chaine � afficher pour le champ
	 */
	public void setString(final String fieldName, final String value) {
		setValue(fieldName, PublisherFieldType.String, value);
	}

	/**
	 * Fixe une valeur bool�enne.
	 * @param fieldName Nom du champ 
	 * @param value Valeur bool�enne du champ
	 */
	public void setBoolean(final String fieldName, final boolean value) {
		setValue(fieldName, PublisherFieldType.Boolean, value);
	}

	/**
	 * Fixe un champ de type objet.
	 * @param fieldName Nom du champ 
	 * @param node Noeud
	 */
	public void setNode(final String fieldName, final PublisherNode node) {
		setValue(fieldName, PublisherFieldType.Node, node);
	}

	/** 
	 * Param�trage d'une image pr�sente dans le mod�le. 
	 * @param fieldName Code de l'image, tel qu'on le retrouve dans le mod�le
	 * @param image Fichier image
	 */
	public void setImage(final String fieldName, final KFile image) {
		setValue(fieldName, PublisherFieldType.Image, image);
	}

	/**
	 * Ajoute un �l�ment dans une liste.
	 * @param fieldName Nom du champ 
	 * @param nodes Element � ajouter dans la liste du champ
	 */
	public void setNodes(final String fieldName, final List<PublisherNode> nodes) {
		setValue(fieldName, PublisherFieldType.List, nodes);
	}

	private void setValue(final String fieldName, final PublisherFieldType fieldType, final Object value) {
		check(fieldType, fieldName, value);
		//---------------------------------------------------------------------
		dataMap.put(fieldName, value);
	}

	//-------------------------------------------------------------------------
	//------------------------ToString-----------------------------------------
	//-------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public String toString() {
		//surcharge de toString pour aider au d�buggage.
		//Notament pour v�rifier l'affectation correct des champs.
		return toString("");
	}

	private String toString(final String tab) {
		final StringBuilder sb = new StringBuilder();
		for (final PublisherField field : nodeDefinition.getFields()) {
			sb.append("\n").append(tab);
			sb.append(field.getName()).append("=");
			final Object data = dataMap.get(field.getName());
			if (data instanceof PublisherNode) {
				sb.append(" {");
				sb.append("\n\t").append(tab);
				sb.append(((PublisherNode) data).toString(tab + "\t"));
				sb.append(" }");
			} else if (data instanceof KFile) {
				sb.append("KFile ").append(((KFile) data).getFileName());
			} else if (data instanceof List) {
				sb.append(" {");
				for (final PublisherNode publisherNode : (List<PublisherNode>) data) {
					sb.append("\n\t").append(tab);
					sb.append(publisherNode.toString(tab + "\t"));
					sb.append(", ");
				}
				sb.append(" }");
			} else {
				sb.append(data);
			}
			sb.append(";");
		}
		return sb.toString();
	}
}
