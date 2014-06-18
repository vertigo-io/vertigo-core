package io.vertigo.dynamox.domain.constraint;

import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractConstraintImpl;
import io.vertigo.kernel.lang.MessageText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exemple de contrainte utilisant une expression régulière.
 *
 * @author  pchretien
 */
public final class ConstraintRegex extends AbstractConstraintImpl<String, String> {
	private Pattern pattern;

	public ConstraintRegex(final String urn) {
		super(urn);
	}

	/**
	 * @param regex Expression régulière
	 */
	@Override
	public void initParameters(final String regex) {
		pattern = Pattern.compile(regex);
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final String value) {
		if (value == null) {
			return true;
		}
		final String input = value;
		final Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_REGEXP, pattern.pattern());
		//return "Pas cohérent avec la regex : " + pattern.pattern();
	}

	/** {@inheritDoc} */
	public Property getProperty() {
		return DtProperty.REGEX;
	}

	/** {@inheritDoc} */
	public String getPropertyValue() {
		return pattern.pattern();
	}

	/**
	 * @return Expression régulière testée par la contrainte
	 */
	public String getRegex() {
		//regex ==>
		return pattern.pattern();
	}
}
