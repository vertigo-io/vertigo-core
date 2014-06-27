package io.vertigo.publisher.impl.merger.grammar;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.publisher.model.PublisherNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe abstraite repr�sentant un KScriptTag.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AbstractKScriptTag.java,v 1.4 2013/10/22 10:49:59 pchretien Exp $
 */
abstract class AbstractKScriptTag {
	/**
	 * JSP start bloc.
	 */
	protected static final String START_BLOC_JSP = "&lt;%";
	/**
	 * END_BLOC_JSP.
	 */
	protected static final String END_BLOC_JSP = "%&gt;";
	/**
	 * FIELD_PATH_CALL.
	 */
	protected static final String FIELD_PATH_CALL = "^([0-9a-zA-Z_]+(?:\\.[0-9a-zA-Z_]+)*)";
	/**
	* FIELD_PATH_CALL_CONDITIONAL.
	* ADU - 20120529 : modification du pattern pour acceptation espaces et accents.
	*/
	protected static final String FIELD_PATH_CALL_EQUALS_CONDITION = "^([0-9a-zA-Z_]+(?:\\.[0-9a-zA-Z_]+)*)=(\\&quot;|\")(.*)(\\&quot;|\")";

	/**
	 * Ajouter l'appel de la methode getStringValue sur un fieldPath.
	 *
	 * @param fieldPath fieldPath du champ recherch�
	 * @param currentVariableName nom de la variable local courante
	 * @return Appel de la methode report
	 */
	protected final String getCallForFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getString(\"" + fieldPath + "\")";
	}

	/**
	 * Ajouter l'appel de la methode getBooleanValue sur un fieldPath.
	 *
	 * @param fieldPath fieldPath du champ recherch�
	 * @param currentVariableName nom de la variable local courante
	 * @return Appel de la methode report
	 */
	protected final String getCallForBooleanFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getBoolean(\"" + fieldPath + "\")";
	}

	/**
	 * Ajouter du test equals d'un fieldPath et d'une valeur fixe du mod�le.
	 * 
	 * @param fieldPath Chemin du champ.
	 * @param value Valeur � tester.
	 * @param currentVariableName Nom de la variable courante
	 * @return Code java resultant.
	 */
	protected final String getCallForEqualsBooleanFieldPath(final String fieldPath, final String value, final String currentVariableName) {
		final StringBuilder concatString = new StringBuilder();
		concatString.append(getCallForFieldPath(fieldPath, currentVariableName));
		concatString.append(".equals(\"");
		concatString.append(value);
		concatString.append("\")");
		return concatString.toString();
	}

	/**
	 * Ajouter l'appel de la methode getNodes sur un fieldPath.
	 * @param fieldPath Chemin du champ.
	 * @param currentVariableName Nom de la variable courante
	 * @return Code java resultant.
	 */
	protected final String getCallForCollectionFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getNodes(\"" + fieldPath + "\")";
	}

	/**
	 * Ajouter l'appel de la methode getNode sur un fieldPath.
	 * @param fieldPath Chemin du champ.
	 * @param currentVariableName Nom de la variable courante
	 * @return Code java resultant.
	 */
	protected final String getCallForObjectFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getNode(\"" + fieldPath + "\")";
	}

	/**
	 * @return Class d'acc�s aux donn�es.
	 */
	protected final Class<?> getDataAccessorClass() {
		return PublisherNode.class;
	}

	/**
	 * Injecte les donn�es dans la chaine de caractere de rendu d'un tag.
	 *
	 * @param tagRepresentation la chaine represantant un tag donn�
	 * @param datas Donn�es sous forme de chaines de caractere
	 * @return Tag java repr�sent� sous forme de chaine de caractere
	 */
	protected final String getTagRepresentation(final String tagRepresentation, final String[] datas) {
		return START_BLOC_JSP + StringUtil.format(tagRepresentation, (Object[]) datas) + END_BLOC_JSP;
	}

	/**
	 * Permet de v�rifier le format d'un attribut de tag et de le parser.
	 *
	 * @param attribute l'attribut d'un tag
	 * @param regEexpFormat le format dans lequel il doit etre ecrit
	 * @return la list des groupe de l'expression reguliere(chaine entre
	 *         parentheses dans l'expression reguliere) ou null si cela ne
	 *         matche pas le format. la premiere case du tableau correspond
	 *         toujours a la chaine � l'attribut lui m�me
	 */
	protected final String[] parseAttribute(final String attribute, final String regEexpFormat) {
		Assertion.checkNotNull(attribute);
		Assertion.checkNotNull(regEexpFormat);
		//----------------------------------------------------

		final String[] groups;
		int nbGroup = 0;

		final Pattern pattern = Pattern.compile(regEexpFormat);
		final Matcher matcher = pattern.matcher(attribute);

		if (!matcher.matches()) {
			throw new VRuntimeException("attribut \"{0}\" mal form� (ne respect pas le format {1})", null, attribute, regEexpFormat);
		}

		nbGroup = matcher.groupCount();
		if (nbGroup > 0) {
			groups = new String[nbGroup + 1];

			for (int i = 0; i <= nbGroup; i++) {
				groups[i] = matcher.group(i);
			}
		} else {
			groups = new String[1];
			groups[0] = attribute;
		}

		return groups;
	}

}
