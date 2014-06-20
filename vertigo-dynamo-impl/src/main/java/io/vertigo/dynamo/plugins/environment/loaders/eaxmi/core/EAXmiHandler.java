package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import io.vertigo.kernel.lang.Assertion;

import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler de lecture du fichier XMI g�n�r� par Enterprise Architect.
 * La m�thode de lecture est directement d�pendantes des extensions EA 
 * pour les informations des �l�ments.
 * @author pforhan
 *
 */
public final class EAXmiHandler extends DefaultHandler {
	private static final String ATTR_ID = "xmi:id";
	private static final String ATTR_REF = "xmi:idref";
	private static final String ATTR_TYPE = "xmi:type";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_ASSOCIATION = "association";

	private final Map<EAXmiId, EAXmiObject> map;

	private EAXmiObject currentObject;

	// La premi�re phase d�finit les objets, les attributs, les associations
	// La deuxi�me phase remplit les prorpi�ts des objets.
	private boolean phase2 = false;

	private final Logger log = Logger.getLogger(this.getClass());

	EAXmiHandler(final Map<EAXmiId, EAXmiObject> map) {
		Assertion.checkNotNull(map);
		//---------------------------------------------------------------------
		this.map = map;
		this.currentObject = EAXmiObject.createdRoot();
	}

	/** {@inheritDoc} */
	@Override
	public void startElement(final String unusedUri, final String unusedLocalName, final String name, final Attributes attributes) {
		log.debug(" D�but de tag : " + name);
		// Type xmi du tag
		String typeElement = attributes.getValue(ATTR_TYPE);
		// Si le type est null, alors on se base sur le nom du tag (cas des extensions EA)
		if (typeElement == null) {
			typeElement = name;
		}
		log.debug("Type : " + typeElement);

		//Les r�f�rences 
		final String ref = attributes.getValue(ATTR_REF);

		if (ref != null && typeElement != null && EAXmiType.isNodeByRef(typeElement)) {
			phase2 = true;
			log.debug("On est dans la ref�rence " + name + " ref : " + ref);
			// Si le tag courant est associ� � un objet alors on ajoute � cet objet la r�f�rence.
			final EAXmiId eaXmiId = new EAXmiId(ref);
			if (map.containsKey(eaXmiId)) {
				currentObject = map.get(eaXmiId);
				log.debug("Current Object : " + currentObject.getName());
			}
			// On ne g�re que les �l�ments objets qui nous int�ressent	
		} else if (EAXmiType.isObjet(typeElement, name)) {
			log.debug("On est dans l'objet ");
			final String id = attributes.getValue(ATTR_ID);
			final String leNom = attributes.getValue(ATTR_NAME);
			final EAXmiType type = EAXmiType.getType(typeElement);
			final String association = attributes.getValue(ATTR_ASSOCIATION);
			// On a un type, un id et on n'est pas dans un attribut ajout� � cause d'une association.
			if (type != null && id != null && !(type.isAttribute() && association != null)) {
				//Il existe un nouvel objet g�r� associ� � ce Tag
				final EAXmiId eaxmiid = new EAXmiId(id);
				final EAXmiObject obj;
				// Nouvelle classe ou association, on revient au package pour cr�er l'objet.
				if (currentObject.getType() != null && currentObject.getType().isClass() && type.isClass()) {
					obj = currentObject.getParent().createEAXmiObject(eaxmiid, type, leNom);
				} else {
					obj = currentObject.createEAXmiObject(eaxmiid, type, leNom);
				}
				map.put(eaxmiid, obj);
				currentObject = obj;

			}
			// On peut �tre dans le cas d'un d�but de tag utile, on le passe pour le traiter.
		} else if (currentObject != null) {
			currentObject.setProperty(name, "", attributes);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void endElement(final String unusedUri, final String unusedLocalName, final String name) {
		// Si c'est un attribut l'objet courant, on revient � la classe qui le contient.
		if (currentObject.getType() != null && currentObject.getType().isAttribute() && !phase2) {
			currentObject = currentObject.getParent();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void fatalError(final org.xml.sax.SAXParseException e) throws SAXException {
		throw e;
	}

}
