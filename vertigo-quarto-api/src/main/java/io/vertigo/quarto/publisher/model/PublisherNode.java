/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.quarto.publisher.model;

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.quarto.publisher.metamodel.PublisherField;
import io.vertigo.quarto.publisher.metamodel.PublisherFieldType;
import io.vertigo.quarto.publisher.metamodel.PublisherNodeDefinition;
import io.vertigo.util.StringUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conteneur de données utilisé par Publisher au sein d'un PublisherData.
 *
 * Implémentation d'un noeud dans une structure PublisherData.
 * Un noeud contient des champs.
 * Les champs peuvent être :
 * - soit simple (valués) et de type Boolean, String ou Image
 * - soit un autre noeud,
 * - soit une liste de noeud.
 *
 * @author npiedeloup, pchretien
 */
public final class PublisherNode implements Serializable {
	private static final long serialVersionUID = 2174855665059480516L;

	private final PublisherNodeDefinition nodeDefinition;
	private final Map<String, Object> dataMap = new HashMap<>();

	PublisherNode(final PublisherNodeDefinition nodeDefinition) {
		Assertion.checkNotNull(nodeDefinition);
		//-----
		this.nodeDefinition = nodeDefinition;
		//On initialise toutes les champs de type liste à vide.
		for (final PublisherField field : getNodeDefinition().getFields()) {
			if (PublisherFieldType.List.equals(field.getFieldType())) {
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

	private void check(final PublisherFieldType fieldType, final String fieldName, final Object value) {
		Assertion.checkNotNull(fieldName, "Le nom du champ est obligatoire.");
		Assertion.checkNotNull(value, "La valeur du champ {0} est obligatoire.", fieldName);
		//-----
		final PublisherFieldType currentFieldType = nodeDefinition.getField(fieldName).getFieldType();
		Assertion.checkArgument(currentFieldType == fieldType, "Le field {0} n''est pas du type {1} mais de type {2}", fieldName, fieldType, currentFieldType);
		Assertion.checkArgument(fieldType.checkValue(value), "La valeur {0} n'est pas conforme au type '{1}' sur le champ '{2}'", value, fieldType, fieldName);
	}

	/**
	 * Crée un PublisherNodeData pour un champs donnée.
	 * @param fieldName Nom du champ.
	 * @return PublisherNodeDataWritable pour ce champ.
	 */
	public PublisherNode createNode(final String fieldName) {
		final Option<PublisherNodeDefinition> childNodeDefinition = nodeDefinition.getField(fieldName).getNodeDefinition();
		Assertion.checkArgument(childNodeDefinition.isDefined(), "Le champ {0} n'accepte pas l'ajout de noeud", fieldName);
		return new PublisherNode(childNodeDefinition.get());
	}

	//=========================================================================
	//-----GetValue
	//=========================================================================
	/**
	 * Récupère une valeur affichable.
	 * @param fieldName Nom du champ
	 * @return Chaine à afficher pour le champ
	 */
	public String getString(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.String, String.class);
	}

	/**
	 * Récupère une valeur booléenne.
	 * @param fieldName Nom du champ
	 * @return Valeur booléenne du champ
	 */
	public boolean getBoolean(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.Boolean, Boolean.class);
	}

	/**
	 * Permet de récupérer un champs de type objet.
	 * @param fieldName Nom du champ
	 * @return PublisherDataNode porté par ce champ
	 */
	public PublisherNode getNode(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.Node, PublisherNode.class);
	}

	/**
	 * Permet de récupérer un champs de type image.
	 * @param fieldName Code de l'image
	 * @return Image portée par ce champ
	 */
	public VFile getImage(final String fieldName) {
		return getValue(fieldName, PublisherFieldType.Image, VFile.class);
	}

	/**
	 * Récupère la liste des noeuds d'un champlistes.
	 * @param fieldName Nom du champ
	 * @return Liste de PublisherDataNode portée par ce champ
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
		//-----
		return clazz.cast(value);
	}

	private <C> C getValueByPath(final int pathIndex, final String fieldPath, final PublisherFieldType fieldType, final Class<C> clazz) {
		final String nodeName = fieldPath.substring(0, pathIndex);
		final PublisherNode node;
		try {
			node = getNode(nodeName);
		} catch (final NullPointerException e) {//on catch juste pour reformuler l'erreur
			if (e.getMessage().contains("est obligatoire")) {
				final String msg = "L'objet {0} utilisé pour le champs {1} est null. Dans votre modèle vous devez tester l'existance de cet objet.";
				throw new NullPointerException(StringUtil.format(msg, nodeName, fieldPath));
			}
			throw e;
		}
		return node.getValue(fieldPath.substring(pathIndex + 1), fieldType, clazz);
	}

	//=========================================================================
	//-----SetValue
	//=========================================================================
	/**
	 * Fixe une valeur affichable.
	 * @param fieldName Nom du champ
	 * @param value Chaine à afficher pour le champ
	 */
	public void setString(final String fieldName, final String value) {
		setValue(fieldName, PublisherFieldType.String, value);
	}

	/**
	 * Fixe une valeur booléenne.
	 * @param fieldName Nom du champ
	 * @param value Valeur booléenne du champ
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
	 * Paramétrage d'une image présente dans le modèle.
	 * @param fieldName Code de l'image, tel qu'on le retrouve dans le modèle
	 * @param image Fichier image
	 */
	public void setImage(final String fieldName, final VFile image) {
		setValue(fieldName, PublisherFieldType.Image, image);
	}

	/**
	 * Ajoute un élément dans une liste.
	 * @param fieldName Nom du champ
	 * @param nodes Element à ajouter dans la liste du champ
	 */
	public void setNodes(final String fieldName, final List<PublisherNode> nodes) {
		setValue(fieldName, PublisherFieldType.List, nodes);
	}

	private void setValue(final String fieldName, final PublisherFieldType fieldType, final Object value) {
		check(fieldType, fieldName, value);
		//-----
		dataMap.put(fieldName, value);
	}

	//=========================================================================
	//-----ToString
	//=========================================================================

	/** {@inheritDoc} */
	@Override
	public String toString() {
		//surcharge de toString pour aider au débuggage.
		//Notament pour vérifier l'affectation correct des champs.
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
			} else if (data instanceof VFile) {
				sb.append("VFile ").append(((VFile) data).getFileName());
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
