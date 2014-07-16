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
package io.vertigo.labs.plugins.geocoder.google;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.labs.geocoder.GeoLocation;
import io.vertigo.labs.impl.geocoder.GeoCoderPlugin;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author spoitrenaud
 * 
 */
public final class GoogleGeoCoderPlugin implements GeoCoderPlugin {
	// D�but de la requ�te http
	private static final String GEOCODE_REQUEST_PREFIX = "http://maps.google.com/maps/api/geocode/xml";
	// Expression XPath permettant de r�cup�rer la latitude, la longitude et
	// l'adresse format�e de
	// l'adresse � g�olocaliser
	private static final String XPATH_LATITUDE = "//result//geometry//location//lat";
	private static final String XPATH_LONGITUDE = "//result//geometry//location//lng";
	//	private static final String XPATH_FORMATTED_ADDRESS = "//formatted_address";
	//	private static final String XPATH_ACCURACY = "//result/type";
	private static final String XPATH_ADDRESSES = "//address_component";
	private static final String XPATH_STATUS = "//status";
	private final Option<Proxy> proxy;
	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	//	/**
	//	 * M�thode d''initialisation de l'API.
	//	 */
	@Inject
	public GoogleGeoCoderPlugin(final @Named("proxyHost") Option<String> proxyHost, @Named("proxyPort") final Option<String> proxyPort) {
		Assertion.checkNotNull(proxyHost);
		Assertion.checkNotNull(proxyPort);
		Assertion.checkArgument((proxyHost.isDefined() && proxyPort.isDefined()) || (proxyHost.isEmpty() && proxyPort.isEmpty()), "les deux param�tres host et port doivent �tre tous les deux remplis ou vides");
		//-----------------------------------------------------------------------
		if (proxyHost.isDefined()) {
			proxy = Option.some(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.get(), Integer.parseInt(proxyPort.get()))));
		} else {
			proxy = Option.none();
		}
	}

	/**
	 * R�cup�ration d'une connexion.
	 * 
	 * @param url type URL
	 * @return type Document contenant les r�sultats de la requ�te
	 */
	private HttpURLConnection createConnection(final URL url) {
		try {
			return doCreateConnection(url);
		} catch (final IOException e) {
			throw new RuntimeException("Erreur de connexion au service (HTTP)", e);
		}
	}

	private HttpURLConnection doCreateConnection(final URL url) throws IOException {
		Assertion.checkNotNull(url);
		//---------------------------------------------------------------------------
		HttpURLConnection connection;
		if (proxy.isDefined()) {
			connection = (HttpURLConnection) url.openConnection(proxy.get());
		} else {
			connection = (HttpURLConnection) url.openConnection();
		}
		connection.setDoOutput(true);
		return connection;
	}

	/**
	 * M�thode de connexion au service Google.
	 * 
	 * @param address Cha�ne de caract�res contenant l'adresse � geocoder
	 * @return Document
	 */
	private Document geoCode(final String address) {
		Assertion.checkNotNull(address);
		//---------------------------------------------------------------------------
		final String urlString;
		try {
			urlString = GEOCODE_REQUEST_PREFIX + "?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=false";
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Erreur lors de l'encodage de l'adresse", e);
		}

		final URL url;
		try {
			url = new URL(urlString);
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Erreur lors de la creation de l'URL", e);
		}

		final HttpURLConnection connection = createConnection(url);
		try {
			// Connexion et r�cup�ration des r�sultats
			connection.connect();
			final InputSource geocoderResultInputSource = new InputSource(connection.getInputStream());

			// Lecture des r�sultats sous forme XML
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);
		} catch (final IOException e) {
			throw new RuntimeException("Erreur de connexion au service", e);
		} catch (final SAXException e) {
			throw new RuntimeException("Erreur lors de la r�cuperation des r�sultats de la requ�te", e);
		} catch (final ParserConfigurationException e) {
			throw new RuntimeException("Erreur de configuration du parseur XML", e);
		} finally {
			connection.disconnect();
		}
	}

	/**
	 * Parseur XML avec l'expression XPath.
	 * 
	 * @param xml : le document XML r�cup�r� depuis Google Geocoder
	 * @param xPathString : l'expression XPath permettant de parser le XML 
	 * @return NodeList contenant les donn�es du fichier XML
	 */
	private NodeList findNodes(final Document xml, final String xPathString) {
		Assertion.checkNotNull(xml);
		Assertion.checkArgNotEmpty(xPathString);
		//---------------------------------------------------------------------------
		final XPath xpath = xPathFactory.newXPath();

		try {
			return (NodeList) xpath.evaluate(xPathString, xml, XPathConstants.NODESET);
		} catch (final XPathExpressionException ex) {
			throw new RuntimeException("Erreur lors du Parsing XML", ex);
		}
	}

	/**
	* Parseur XML avec l'expression XPath.
	* 
	* @param xml : le document XML r�cup�r� depuis Google Geocoder
	* @param xPathString : l'expression XPath permettant de parser le XML 
	* @return Node contenant les donn�es du fichier XML
	*/
	private Node findNode(final Document xml, final String xPathString) {
		Assertion.checkNotNull(xml);
		Assertion.checkArgNotEmpty(xPathString);
		//---------------------------------------------------------------------------
		final XPath xpath = xPathFactory.newXPath();
		try {
			return (Node) xpath.evaluate(xPathString, xml, XPathConstants.NODE);
		} catch (final XPathExpressionException ex) {
			throw new RuntimeException("Erreur lors du Parsing XML", ex);
		}
	}

	public static String toString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

	/** {@inheritDoc} */
	public GeoLocation findLocation(final String address) {
		Assertion.checkNotNull(address);
		//---------------------------------------------------------------------------
		final Document geocoderResultDocument = geoCode(address);
		if (geocoderResultDocument == null) {
			throw new RuntimeException("Pas de r�ponse du service");
		}
		//---------------------------------------------------------------------
		// 0- V�rification du status 
		final Node StatusNode = findNode(geocoderResultDocument, XPATH_STATUS);
		if (!"OK".equals(StatusNode.getTextContent().trim())) {
			return GeoLocation.UNDEFINED;
		}

		// 1- Parsing du XML
		final Node latitudeNode = findNode(geocoderResultDocument, XPATH_LATITUDE);
		final Node longitudeNode = findNode(geocoderResultDocument, XPATH_LONGITUDE);
		//final Node formattedAddressNode = findNode(geocoderResultDocument, XPATH_FORMATTED_ADDRESS);
		//		final Node accuracyNode = findNode(geocoderResultDocument, XPATH_ACCURACY);
		final NodeList addressNodes = findNodes(geocoderResultDocument, XPATH_ADDRESSES);
		//---------------------------------------------------------------------
		// 2- Typage des donn�es
		//		System.out.println(">>address : " + address);
		//		System.out.println(">>>>>> : " + toString(geocoderResultDocument));
		//		System.out.println(">>longitudeNode : " + longitudeNode);
		final Double latitude = Double.valueOf(latitudeNode.getTextContent().trim());
		final Double longitude = Double.valueOf(longitudeNode.getTextContent().trim());

		//	final String formattedAddress = formattedAddressNode.getTextContent();
		//		final String accuracy = accuracyNode.getTextContent();

		//	Map<GeoLocation.Level, String> codes = findCodes(addressNodes);
		//---------------------------------------------------------------------
		// 2- Cas des adresses dites "political"
		//		<address_component>
		//			<long_name>France</long_name>
		//			<short_name>FR</short_name>
		//			<type>country</type>
		//			<type>political</type>
		//		</address_component>
		//----------------------------------------
		String countryCode = null;
		String level1 = null;
		String level2 = null;
		String locality = null;
		for (int i = 0; i < addressNodes.getLength(); i++) {
			Node addressNode = addressNodes.item(i);
			String shortName = null;
			boolean isCountry = false;
			boolean isAdministrative_area_level_1 = false;
			boolean isAdministrative_area_level_2 = false;
			boolean isLocality = false;

			for (int j = 0; j < addressNode.getChildNodes().getLength(); j++) {
				Node node = addressNode.getChildNodes().item(j);
				if ("short_name".equals(node.getNodeName())) {
					shortName = node.getTextContent().trim();
				} else if ("type".equals(node.getNodeName())) {
					if ("country".equals(node.getTextContent())) {
						isCountry = true;
					} else if ("administrative_area_level_1".equals(node.getTextContent())) {
						isAdministrative_area_level_1 = true;
					} else if ("administrative_area_level_2".equals(node.getTextContent())) {
						isAdministrative_area_level_2 = true;
					} else if ("locality".equals(node.getTextContent())) {
						isLocality = true;
					}
				}
			}
			if (isCountry) {
				countryCode = shortName;
			} else if (isAdministrative_area_level_1) {
				level1 = shortName;
			} else if (isAdministrative_area_level_2) {
				level2 = shortName;
			} else if (isLocality) {
				locality = shortName;
			}
		}
		//---------------------------------------------------------------------
		// 3- Cr�ation du r�sultat :  GeoLocation
		System.out.println(">>address : " + address);
		System.out.println("		>>level1 : " + level1);
		System.out.println("		>>level2 : " + level2);

		return new GeoLocation(latitude, longitude, countryCode, level1, level2, locality);
	}
}
