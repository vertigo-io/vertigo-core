package io.vertigo.struts2.impl.formatter;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractFormatterImpl;
import io.vertigo.dynamox.domain.formatter.Resources;

/**
 * Gestion des formattages des identifiants.
 *
 * @author pchretien
 */
public final class FormatterId extends AbstractFormatterImpl {
	/**
	 * Constructeur.
	 * @param name Nom du formatteur
	 */
	public FormatterId(final String name) {
		super(name);
	}

	/** {@inheritDoc} */
	@Override
	public void initParameters(final String args) {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	public Object stringToValue(final String strValue, final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.Long, "Formatter ne s'applique qu'aux Long");
		//----------------------------------------------------------------------
		try {
			if (StringUtil.isEmpty(strValue)) {
				return null;
			}
			return Long.parseLong(strValue.trim());
		} catch (final NumberFormatException e) {
			// cas des erreurs sur les formats de nombre
			throw new FormatterException(Resources.DYNAMOX_NUMBER_NOT_FORMATTED, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.Long, "Formatter ne s'applique qu'aux Long");
		//----------------------------------------------------------------------
		if (objValue == null) {
			return "";
		}
		return String.valueOf(objValue);
	}
}
