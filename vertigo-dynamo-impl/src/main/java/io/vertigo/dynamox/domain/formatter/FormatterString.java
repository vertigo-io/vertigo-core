package io.vertigo.dynamox.domain.formatter;

import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractFormatterImpl;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

/**
 * Gestion des formattages de String.
 *
 * @author pchretien
 * @version $Id: FormatterString.java,v 1.3 2013/10/22 11:00:06 pchretien Exp $
 */
public final class FormatterString extends AbstractFormatterImpl {
	/**
	 * Mode utilisé.
	 * Pour tous les mode un "trim" à droite et à gauche est effectué.
	 * Le trim à droite est obligatoire.
	 * Concernant le trim à gauche, il est possible de s'en passer 
	 * il convient alors de créer un formatter ad hoc. 
	 */
	public static enum Mode {
		/**
		 * Aucun formattage.
		 */
		BASIC,
		/**
		 * Met en majuscules toutes les lettres.
		 */
		UPPER,
		/**
		 * Met en minuscules toutes les lettres.
		 */
		LOWER,
		/**
		 * Met en majuscules les premières lettres de chaque mot et en minuscules les suivantes
		 * Les séparateurs utilisés sont l'espace, "_" et "-.
		 */
		UPPER_FIRST
	}

	private Mode mode;

	/**
	 * Constructeur.
	 * @param name Nom du formatteur
	 */
	public FormatterString(final String name) {
		super(name);
	}

	/** {@inheritDoc} */
	@Override
	public void initParameters(final String args) {
		//Si args non renseigné on prend le mode par défaut
		mode = args == null ? Mode.BASIC : Mode.valueOf(args);
	}

	/** {@inheritDoc} */
	public Object stringToValue(final String strValue, final KDataType dataType) {
		Assertion.checkArgument(dataType == KDataType.String, "Formatter ne s'applique qu'aux Strings");
		//----------------------------------------------------------------------
		return apply(strValue);
	}

	/** {@inheritDoc} */
	public String valueToString(final Object objValue, final KDataType dataType) {
		Assertion.checkArgument(dataType == KDataType.String, "Formatter ne s'applique qu'aux Strings");
		//----------------------------------------------------------------------
		final String result = apply((String) objValue);
		if (result == null) {
			return "";
		}
		return result;
	}

	private String apply(final String strValue) {
		final String result;
		final String sValue = StringUtil.isEmpty(strValue) ? null : strValue.trim();

		if (sValue == null) {
			result = null;
		} else {
			switch (mode) {
				case BASIC:
					result = sValue;
					break;
				case UPPER:
					result = sValue.toUpperCase();
					break;
				case LOWER:
					result = sValue.toLowerCase();
					break;
				case UPPER_FIRST:
					result = firstLetterUpper(sValue);
					break;
				default:
					throw new IllegalAccessError("cas non implémenté");
			}
		}
		return result;
	}

	private static String firstLetterUpper(final String str) {
		final char[] letters = str.toCharArray();
		letters[0] = Character.toUpperCase(letters[0]);
		for (int i = 1; i < letters.length; i++) {
			if (letters[i - 1] == ' ' || letters[i - 1] == '-' || letters[i - 1] == '_') {
				letters[i] = Character.toUpperCase(letters[i]);
			} else {
				letters[i] = Character.toLowerCase(letters[i]);
			}
		}
		return new String(letters);
	}
}
