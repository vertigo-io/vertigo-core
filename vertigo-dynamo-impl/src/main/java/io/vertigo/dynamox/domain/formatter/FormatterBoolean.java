package io.vertigo.dynamox.domain.formatter;

import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractFormatterImpl;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.util.StringTokenizer;

/**
 * Gestion des formattages de booléens.
 *
 * Args contient deux arguments séparés par des points virgules ';'
 * Le premier argument est obligatoire il représente le format d'affichage de la valeur vraie.
 *
 * Exemple 1 d'arguments si les ressources ne sont pas toutes externalisées : "OUI ; NON"
 * Exemple 2 d'arguments si les ressources sont externalisées : "TXT_OUI ; TXT_NON"
 *
 * @author pchretien
 */
public final class FormatterBoolean extends AbstractFormatterImpl {
	/**
	 * MessageText pour les boolean à true
	 */
	private String truePattern;

	/**
	 * MessageText pour les boolean à false
	 */
	private String falsePattern;

	/**
	 * Constructeur.
	 * @param name Nom du formatteur
	 */
	public FormatterBoolean(final String name) {
		super(name);
	}

	/** {@inheritDoc} */
	@Override
	public void initParameters(final String args) {
		// Les arguments ne doivent pas être vides.
		assertArgs(args != null);
		//----------------------------------------------------------------------
		final StringTokenizer st = new StringTokenizer(args, ";");

		//OUI
		assertArgs(st.hasMoreTokens());
		truePattern = st.nextToken().trim();

		//NON
		assertArgs(st.hasMoreTokens());
		falsePattern = st.nextToken().trim();

		//C'est fini plus de texte attendu
		assertArgs(!st.hasMoreTokens());
	}

	private static void assertArgs(final boolean test) {
		Assertion.checkArgument(test, "Les arguments pour la construction de FormatterBoolean sont invalides :format oui; format non");
	}

	/** {@inheritDoc} */
	public String valueToString(final Object objValue, final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.Boolean, "Formatter ne s'applique qu'aux booléens");
		//----------------------------------------------------------------------
		return booleanToString((Boolean) objValue);
	}

	/** {@inheritDoc} */
	public Object stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		Assertion.checkArgument(dataType == DataType.Boolean, "Formatter ne s'applique qu'aux booléens");
		//----------------------------------------------------------------------
		final String sValue = StringUtil.isEmpty(strValue) ? null : strValue.trim();

		return stringToBoolean(sValue);
	}

	/**
	 * Convertit une valeur String en booléen.
	 * @param booleanString valeur booléenne sous forme de chaine
	 * @return valeur typée en Boolean.
	 * @throws FormatterException Erreur de parsing
	 */
	private Boolean stringToBoolean(final String booleanString) throws FormatterException {
		final Boolean booleanValue;
		if (null == booleanString) {
			booleanValue = null;
		} else if ("true".equals(booleanString) || "1".equals(booleanString) || truePattern.equals(booleanString)) {
			booleanValue = Boolean.TRUE;
		} else if ("false".equals(booleanString) || "0".equals(booleanString) || falsePattern.equals(booleanString)) {
			booleanValue = Boolean.FALSE;
		} else {
			throw new FormatterException(Resources.DYNAMOX_BOOLEAN_NOT_FORMATTED);
		}
		return booleanValue;
	}

	/**
	 * Convertit une valeur boolean en chaine.
	 * @param booleanValue Valeur booléenne
	 * @return Valeur formattée en String
	 */
	private String booleanToString(final Boolean booleanValue) {
		final String boolString;
		if (booleanValue == null) {
			boolString = null;
		} else {
			boolString = booleanValue.booleanValue() ? truePattern : falsePattern;
		}
		return boolString;
	}
}
