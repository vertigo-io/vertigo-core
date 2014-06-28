package io.vertigo.publisher.impl.merger.grammar;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.publisher.impl.merger.script.ScriptContext;
import io.vertigo.publisher.impl.merger.script.ScriptTag;
import io.vertigo.publisher.impl.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 * @version $Id: TagObject.java,v 1.3 2013/10/22 10:49:59 pchretien Exp $
 */
//public car instanci� dynamiquement
public final class TagObject extends AbstractKScriptTag implements ScriptTag {
	private static final String OBJECT_CALL = "\\{ {1} {2} = ({1}) {0}; ";
	private static final String OBJECT_ATTRIBUTE = "^([0-9a-zA-Z_]+) *= *([0-9a-zA-Z_]+(\\.[0-9a-zA-Z_]+)*)";

	/** {@inheritDoc} */
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		// Renvoie un tableau de trois elements d'apr�s l'expression reguliere
		final String[] parsing = parseAttribute(tag.getAttribute(), OBJECT_ATTRIBUTE);

		// le tag est dans le bon format

		/**
		 * on recupere le parsing de l'expression reguliere
		 * parsing[0] -> attribut entier
		 * parsing[1] -> le nom de la variable de l'objet
		 * parsing[2] -> le field path de l'objet
		 */
		context.push(parsing[1]);

		// rendu du tag
		final String[] rendering = new String[3];
		rendering[0] = getCallForObjectFieldPath(parsing[2], tag.getCurrentVariable());
		rendering[1] = getDataAccessorClass().getCanonicalName();
		rendering[2] = parsing[1];

		return getTagRepresentation(OBJECT_CALL, rendering);
	}

	/** {@inheritDoc} */
	public String renderClose(final ScriptTagContent content, final ScriptContext context) {
		if (context.empty()) {
			throw new VRuntimeException("document malform� : le tag object est mal ferm�", null);
		}
		context.pop();
		return START_BLOC_JSP + '}' + END_BLOC_JSP;
	}
}
