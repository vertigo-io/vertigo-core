package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

import java.util.Map;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler SAX, permettant de parser le OOM.
 *
 * @author pchretien
 * @version $Id: HandlerOOM.java,v 1.3 2013/10/22 12:30:19 pchretien Exp $
 */
final class HandlerOOM extends DefaultHandler {
	private static final String ATTR_ID = "Id";
	private static final String ATTR_REF = "Ref";

	private final Map<IdOOM, ObjectOOM> map;

	private TagOOM currentTag;

	private String chars;

	HandlerOOM(final Map<IdOOM, ObjectOOM> map) {
		Assertion.checkNotNull(map);
		//---------------------------------------------------------------------
		this.map = map;
		final ObjectOOM root = ObjectOOM.createdRoot();
		//root.display();
		currentTag = TagOOM.createRootTag(root);
	}

	/** {@inheritDoc} */
	@Override
	public void startElement(final String unusedUri, final String unusedLocalName, final String name, final org.xml.sax.Attributes attributes) {
		//Dans le cas des références si on trouve une référence sur un objet courant alors on l'ajoute.
		final String ref = attributes.getValue(ATTR_REF);
		if (ref != null && TypeOOM.isNodeByRef(name) && currentTag.getCurrentOOM() != null) {
			// Si le tag courant est associé à un objet alors on ajoute à cet objet la référence.
			final IdOOM idOOM = new IdOOM(ref);
			currentTag.getCurrentOOM().addIdOOM(idOOM);
		}

		//Seuls les tags commenéant par o: possède un id et réciproquement
		final boolean isObj = name.startsWith("o:");
		if (isObj) {
			final String id = attributes.getValue(ATTR_ID);
			final TypeOOM type = TypeOOM.getType(name);
			if (type != null && id != null) {
				//Il existe un nouvel objet géré associé à ce Tag
				final IdOOM idOOM = new IdOOM(id);
				final ObjectOOM obj = currentTag.getParentOOM().createObjectOOM(idOOM, type);
				map.put(idOOM, obj);
				currentTag = currentTag.createTag(obj);
			} else {
				//Ce tag ne contient aucun nouvel objet intéressant
				currentTag = currentTag.createTag();
			}
		}
		//Initialisation des contenus de balise
		chars = "";
	}

	/** {@inheritDoc} */
	@Override
	public void endElement(final String unusedUri, final String unusedLocalName, final String name) {
		final boolean isObj = name.startsWith("o:");
		if (isObj) {
			currentTag = currentTag.getParent();
		}
		//======================================================================
		if (currentTag.getCurrentOOM() != null) {
			currentTag.getCurrentOOM().setProperty(name, chars);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void characters(final char[] charArray, final int start, final int length) {
		chars += new String(charArray, start, length);
	}

	/** {@inheritDoc} */
	@Override
	public void fatalError(final org.xml.sax.SAXParseException e) throws SAXException {
		throw e;
	}
}
