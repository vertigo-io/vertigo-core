package io.vertigo.dynamox.domain.formatter;

import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractFormatterImpl;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

/**
 * Implémentation par défaut des formatters.
 * Cette classe est très paramétrable.
 *
 *
 * @author pchretien
 * @version $Id: FormatterDefault.java,v 1.5 2013/10/22 11:00:06 pchretien Exp $
 */
public final class FormatterDefault extends AbstractFormatterImpl {
	private final DefinitionReference<Formatter> booleanFormatterRef;
	private final DefinitionReference<Formatter> numberformatterRef;
	private final DefinitionReference<Formatter> dateFormaterRef;
	private final DefinitionReference<Formatter> stringFormatterRef;

	/**
	 * Constructeur.
	 * @param name Nom du formatteur
	 */
	public FormatterDefault(final String name) {
		super(name);
		final FormatterBoolean booleanFormatter = new FormatterBoolean("FMT_BOOLEAN_DEFAULT");
		final FormatterNumber numberformatter = new FormatterNumber("FMT_NUMBER_DEFAULT");
		final FormatterDate dateFormatter = new FormatterDate("FMT_DATE_DEFAULT");
		final FormatterString stringFormatter = new FormatterString("FMT_STRING_DEFAULT");
		//---
		booleanFormatter.initParameters("True; False");
		numberformatter.initParameters("#,###.##");
		dateFormatter.initParameters("dd/MM/yyyy HH:mm ; dd/MM/yyyy");
		stringFormatter.initParameters(null); //Fonctionnement de base (pas de formatage)
		//---
		booleanFormatterRef = new DefinitionReference<Formatter>(booleanFormatter);
		numberformatterRef = new DefinitionReference<Formatter>(numberformatter);
		dateFormaterRef = new DefinitionReference<Formatter>(dateFormatter);
		stringFormatterRef = new DefinitionReference<Formatter>(stringFormatter);
	}

	/** {@inheritDoc} */
	@Override
	public void initParameters(final String args) {
		// Les arguments doivent être vides.
		Assertion.checkArgument(args == null, "Les arguments pour la construction de FormatterDefault sont invalides");
	}

	/**
	 * 
	 * @param dataType Type 
	 * @return Formatter simple utilisé.
	 */
	public Formatter getFormatter(final KDataType dataType) {
		switch (dataType) {
			case String:
				return stringFormatterRef.get();
			case Date:
				return dateFormaterRef.get();
			case Boolean:
				return booleanFormatterRef.get();
			case Integer:
			case Long:
			case Double:
			case BigDecimal:
				return numberformatterRef.get();
			default:
				throw new IllegalArgumentException(dataType + " n'est pas géré par ce formatter");
		}
	}

	/** {@inheritDoc} */
	public String valueToString(final Object objValue, final KDataType dataType) {
		return getFormatter(dataType).valueToString(objValue, dataType);
	}

	/** {@inheritDoc} */
	public Object stringToValue(final String strValue, final KDataType dataType) throws FormatterException {
		return getFormatter(dataType).stringToValue(strValue, dataType);
	}
}
