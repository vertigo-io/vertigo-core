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

import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.quarto.publisher.impl.merger.processor.ZipUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Classe d'utilitaires pour les fichiers de type DOCX.
 * 
 * @author adufranne
 * @version $Id: DOCXUtil.java,v 1.5 2014/02/27 10:39:24 pchretien Exp $
 */
final class DOCXUtil {
	/** Prefix des fichiers temporaires générés. */
	private static final String TEMP_FILE_PREFIX = "krep";

	/** Suffix des fichiers temporaires générés. */
	private static final String TEMP_FILE_SUFFIX = ".docx";

	/**
	 * Style node for paragraphs.
	 */
	public static final String STYLE_NODE = "w:pPr";
	/**
	 * Pattern matchant un élément.
	 */
	public static final String PATTERN_KSP = "^\\s*(var|loop|if|ifnot|endloop|endvar|endif|endifnot|=|block|endblock).*";

	/**
	 * Requête XPATH retournant tous les noeuds de type "begin" et "end".
	 */
	public static final String XPATH_CLEAN = "//w:r[w:fldChar[@w:fldCharType=\"begin\" or @w:fldCharType=\"end\"]]";
	/**
	 * Requête XPATH retournant tous les bookmarks.
	 */
	public static final String XPATH_CLEAN_BOOKMARKS = "//w:bookmarkEnd | //w:bookmarkStart";
	/**
	 * Requête XPATH retournant tous les noeuds de type "begin".
	 */
	public static final String XPATH_BEGIN = "//w:r[w:fldChar[@w:fldCharType=\"begin\"]]";
	/**
	 * Requête XPATH pour enlever les separate.
	 */
	public static final String XPATH_SEPARATE = "//w:r[w:fldChar[@w:fldCharType=\"separate\"]]";

	/**
	 * Retrouver tous les tags insérés.
	 */
	public static final String XPATH_TAG_NODES = "//w:r[w:instrText]";
	/**
	 * Nom du fichier XML gérant les contenus pour les docx.
	 */
	static final String DOCUMENT_XML_DOCX = "word/document.xml";
	/**
	 * Nom du fichier XML gérant les styles pour les docx.
	 */
	static final String STYLES_XML_DOCX = "word/styles.xml";
	/**
	 * Nom des fichiers XML gérant les headers pour un docx.
	 */
	static final String HEADERS_XML_DOCX = "word/header\\d+\\.xml";
	/**
	 * Nom des fichiers XML gérant les headers pour un docx.
	 */
	static final String FOOTERS_XML_DOCX = "word/footer\\d+\\.xml";
	/**
	 * Pattern pour reconnaitre un champ ksp.
	 */
	static final String KSP_WRAPPING_TAG = "\\s*<#(.*)#>\\s*";

	/**
	 * Enum pour les types de noeuds gérés.
	 * 
	 * @author adufranne
	 * @version $Id: DOCXUtil.java,v 1.5 2014/02/27 10:39:24 pchretien Exp $
	 */
	public enum DOCXNode {
		/**
		 * Begin.
		 */
		BEGIN("begin"),
		/**
		 * end.
		 */
		END("end"),
		/**
		 * Separate.
		 */
		SEPARATE("separate");

		private String ns;

		/**
		 * Constructeur.
		 * 
		 * @param ns le nom.
		 */
		DOCXNode(final String ns) {
			this.ns = ns;
		}

		/**
		 * Getter pour le nom.
		 * 
		 * @return le nom.
		 */
		public String getNs() {
			return ns;
		}
	}

	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private DOCXUtil() {
		super();
	}

	/**
	 * Indique si le contenu d'un noeud appartient à la grammaire KSP.
	 * 
	 * @param content le contenu du noeud.
	 * @return un booléen.
	 */
	public static boolean isWordTag(final String content) {
		final Pattern p = Pattern.compile(PATTERN_KSP);
		final Matcher m = p.matcher(content);
		return !m.matches();
	}

	/**
	 * Extrait les fichiers à modifier d'un docx.
	 * -document
	 * -styles
	 * -footers
	 * -headers
	 * 
	 * @param docxFile ZipFile fichier source
	 * @return une map contenant les noms et les fichiers associés au format texte.
	 * @throws IOException Si une exception d'entrée sortie a lieu
	 */
	public static Map<String, String> extractDOCXContents(final ZipFile docxFile) throws IOException {
		final Map<String, String> xmlContents = new HashMap<>();

		for (final ZipEntry zipEntry : Collections.list(docxFile.entries())) {
			final String entryName = zipEntry.getName();
			if (DOCUMENT_XML_DOCX.equals(entryName)) {
				xmlContents.put(DOCUMENT_XML_DOCX, ZipUtil.readEntry(docxFile, DOCUMENT_XML_DOCX));
			} else if (STYLES_XML_DOCX.equals(entryName)) {
				xmlContents.put(STYLES_XML_DOCX, ZipUtil.readEntry(docxFile, STYLES_XML_DOCX));
			} else if (Pattern.matches(HEADERS_XML_DOCX, entryName)) {
				xmlContents.put(entryName, ZipUtil.readEntry(docxFile, entryName));
			} else if (Pattern.matches(FOOTERS_XML_DOCX, entryName)) {
				xmlContents.put(entryName, ZipUtil.readEntry(docxFile, entryName));
			}
		}

		return xmlContents;

	}

	/**
	 * Crée le fichier content.xml d'un fichier odt par le contenu provenant d'une fusion.
	 * 
	 * @param docxFile ZipFile d'origine
	 * @param newXmlContents Map contenant tous les fichiers qui ont été modifiés.
	 * @return File le document Docx créé.
	 * @throws IOException Si une IOException a lieu
	 */
	public static File createDOCX(final ZipFile docxFile, final Map<String, String> newXmlContents) throws IOException {
		final File resultFile = new TempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
		try (final ZipOutputStream outputFichierDOCX = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(resultFile)))) {
			for (final ZipEntry zipEntry : Collections.list(docxFile.entries())) {
				final String entryName = zipEntry.getName();
				if (newXmlContents.containsKey(entryName)) {
					ZipUtil.writeEntry(outputFichierDOCX, newXmlContents.get(entryName), entryName);
				} else {
					try (final InputStream zipIS = docxFile.getInputStream(zipEntry)) {
						// writeEntry(outputFichierDOCX, zipIS, zipEntry);
						ZipUtil.writeEntry(outputFichierDOCX, ZipUtil.readEntry(docxFile, zipEntry.getName()), zipEntry.getName());
					}
				}
				outputFichierDOCX.closeEntry();
			}
		}
		return resultFile;
	}

	/**
	 * Méthode transformant le Document de travail en String xml.
	 * 
	 * @param xmlDocument le Document à formater.
	 * @return String le xml formaté.
	 */
	public static String renderXML(final Document xmlDocument) {
		final DOMSource domSource = new DOMSource(xmlDocument);
		final StringWriter writer = new StringWriter();
		final StreamResult result = new StreamResult(writer);
		final TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.transform(domSource, result);
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (final TransformerException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	/**
	 * Méthode de chargement d'un Document DOM à partir d'un fichier XML.
	 * 
	 * @param xmlInput la String représentant le fichier XML à traiter.
	 * @return le Document résultant.
	 */
	public static Document loadDOM(final String xmlInput) {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);

		try (final StringReader reader = new StringReader(xmlInput)) {
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			return builder.parse(new InputSource(reader));
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException("Erreur de chargement du fichier XML", e);
		}
	}

	/**
	 * Méthode de chargement d'un objet XPath compatible DOCX.
	 * 
	 * @return l'objet Xpath généré.
	 */
	public static XPath loadXPath() {
		XPath xpath;
		final XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		xpath.setNamespaceContext(new DOCXNamespaceContext());
		return xpath;
	}

}
