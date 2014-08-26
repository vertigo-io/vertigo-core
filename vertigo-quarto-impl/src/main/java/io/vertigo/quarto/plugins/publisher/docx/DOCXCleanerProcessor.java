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
package io.vertigo.quarto.plugins.publisher.docx;

import io.vertigo.quarto.publisher.impl.merger.processor.MergerProcessor;
import io.vertigo.quarto.publisher.model.PublisherData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Cleaner de xml de fichier DOCX.
 * Ce processor effectue plusieurs opérations de rectification du XML d'un fichier DOCX.
 * - 1. Nettoyage du XML en fermant les balises
 * - 2. Suppression des balises de script
 * 
 * @author adufranne
 */
final class DOCXCleanerProcessor implements MergerProcessor {

	private static final String CARRIAGE_RETURN = "\\n";

	private static final String EMPTY_STRING = "\\s+";

	/** {@inheritDoc} */
	public String execute(final String xmlInput, final PublisherData publisherData) {
		final Document xmlDoc = DOCXUtil.loadDOM(xmlInput);
		final XPath xpath = DOCXUtil.loadXPath();
		try {
			removeEmptyTags(xmlDoc, xpath);

			handleCarriageReturn(xmlDoc, xpath);

			return DOCXUtil.renderXML(xmlDoc);

		} catch (final XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Transforme les \n en plusieurs lignes dans le docx.
	 * NB: visibilité package nécessaire pour les tests!.
	 * ex :
	 * <w:p>
	 * <w:pPr>
	 * (style)
	 * < /w:pPr>
	 * <w:r>in<w:r>
	 * <w:r>test\ntest\ntest<w:r>
	 * <w:r>out<w:r>
	 * < /w:p>
	 * Doit se transformer en 3 balises <w:p>, la première contenant le in et un test, la deuxième un test, la troisième
	 * un test et le out. La balise de style
	 * <w:pPr> doit être conservée dans les 3 nouveaux paragraphes.
	 * @param xmlDoc Document source
	 * @param xpath Moteur Xpath
	 * @throws XPathExpressionException Erreur Xpath
	 */
	void handleCarriageReturn(final Document xmlDoc, final XPath xpath) throws XPathExpressionException {

		final NodeList nodeList = (NodeList) xpath.evaluate(DOCXUtil.XPATH_TAG_NODES, xmlDoc, XPathConstants.NODESET);
		Node node;
		String[] lines;

		for (int i = 0; i < nodeList.getLength(); i++) { // pour chaque résultat
			node = nodeList.item(i);

			lines = getNodeTextContent(node);
			if (lines.length <= 1) {
				continue;
			}

			doHandleCarriageReturn(lines, node);
		}

	}

	private String[] getNodeTextContent(final Node node) {
		final String[] lines;
		final String textContent = node.getLastChild().getTextContent();
		if (textContent == null || textContent.isEmpty()) {
			return new String[0];
		}
		lines = textContent.split(CARRIAGE_RETURN);
		return lines;

	}

	private void doHandleCarriageReturn(final String[] lines, final Node node) {
		Node paragraphClone;
		Node rowClone;
		String line;
		Node styleNodeClone;
		Node currentPar = node.getParentNode();
		final List<Node> bufferNodes = new ArrayList<>();
		final List<Node> removeNodes = new ArrayList<>();
		Node saveNode = node;
		final Node parentNode = node.getParentNode().getParentNode();

		node.getLastChild().setTextContent(lines[0]);

		while (saveNode.getNextSibling() != null) { // sauvegarde des noeuds successifs.
			saveNode = saveNode.getNextSibling();
			removeNodes.add(saveNode);
			bufferNodes.add(saveNode.cloneNode(true));
		}
		removeNodes(removeNodes);

		for (int j = 1; j < lines.length; j++) { // pour chaque élément de la ligne splittée.
			line = lines[j];
			// copie du w:p englobant sans ses fils.
			paragraphClone = node.getParentNode().cloneNode(false);
			rowClone = node.cloneNode(true);
			rowClone.getLastChild().setTextContent(line);
			if (DOCXUtil.STYLE_NODE.equals(node.getParentNode().getFirstChild().getNodeName())) { // présence
				// d'un
				// noeud
				// de
				// style
				styleNodeClone = node.getParentNode().getFirstChild().cloneNode(true);
				paragraphClone.appendChild(styleNodeClone);
			}
			paragraphClone.appendChild(rowClone);
			if (currentPar.getNextSibling() == null) { // on a un dernier noeud.
				parentNode.appendChild(paragraphClone);
			} else { // on n'a pas de dernier noeud.
				parentNode.insertBefore(paragraphClone, currentPar.getNextSibling());
			}
			currentPar = currentPar.getNextSibling();
		}
		// ajout des noeuds sauvegardés au dernier élément.
		if (lines.length > 1) {
			for (final Node bufferNode : bufferNodes) {
				currentPar.appendChild(bufferNode);
			}
		}
	}

	private void removeNodes(final List<Node> removeNodes) {
		for (final Node removeNode : removeNodes) {
			removeNode.getParentNode().removeChild(removeNode);
		}
	}

	/**
	 * Enlève les tags d'expression restés vides après traitement.
	 * @param xmlDoc Document source
	 * @param xpath Moteur Xpath
	 * @throws XPathExpressionException Erreur Xpath
	 */
	private void removeEmptyTags(final Document xmlDoc, final XPath xpath) throws XPathExpressionException {
		final NodeList nodeList = (NodeList) xpath.evaluate("//w:r[w:instrText]", xmlDoc, XPathConstants.NODESET);
		Node node;
		final Pattern p = Pattern.compile(EMPTY_STRING);
		Matcher m;

		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			m = p.matcher(node.getLastChild().getTextContent());
			if (m.matches()) {
				node.getParentNode().removeChild(node);
			}

		}
	}
}
