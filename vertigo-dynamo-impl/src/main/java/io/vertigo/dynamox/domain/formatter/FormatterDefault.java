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
package io.vertigo.dynamox.domain.formatter;

import io.vertigo.core.Home;
import io.vertigo.core.definiton.DefinitionReference;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractFormatterImpl;

/**
 * Default formatter for boolean, number, date and string.
 * It's possible to override default formatting by registering a specific formatter with a conventional name.
 * FMT_STRING_DEFAULT, FMT_DATE_DEFAULT, FMT_BOOLEAN_DEFAULT, FMT_NUMBER_DEFAULT
 * Must be declared before this default Formatter !!
 * @author pchretien, npiedeloup
 */
public final class FormatterDefault extends AbstractFormatterImpl {
	private static final String FMT_STRING_DEFAULT = "FMT_STRING_DEFAULT";
	private static final String FMT_DATE_DEFAULT = "FMT_DATE_DEFAULT";
	private static final String FMT_BOOLEAN_DEFAULT = "FMT_BOOLEAN_DEFAULT";
	private static final String FMT_NUMBER_DEFAULT = "FMT_NUMBER_DEFAULT";

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
		booleanFormatterRef = new DefinitionReference<Formatter>(obtainFormatterBoolean());
		numberformatterRef = new DefinitionReference<Formatter>(obtainFormatterNumber());
		dateFormaterRef = new DefinitionReference<Formatter>(obtainFormatterDate());
		stringFormatterRef = new DefinitionReference<Formatter>(obtainFormatterString());
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
	public Formatter getFormatter(final DataType dataType) {
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
			case DataStream:
			case DtList:
			case DtObject:
			default:
				throw new IllegalArgumentException(dataType + " n'est pas géré par ce formatter");
		}
	}

	/** {@inheritDoc} */
	public String valueToString(final Object objValue, final DataType dataType) {
		return getFormatter(dataType).valueToString(objValue, dataType);
	}

	/** {@inheritDoc} */
	public Object stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		return getFormatter(dataType).stringToValue(strValue, dataType);
	}

	private FormatterBoolean obtainFormatterBoolean() {
		if (!Home.getDefinitionSpace().containsKey(FMT_BOOLEAN_DEFAULT)) {
			final FormatterBoolean defaultformatter = new FormatterBoolean(FMT_BOOLEAN_DEFAULT);
			defaultformatter.initParameters("Oui; Non");
			//Home.getDefinitionSpace().put(defaultformatter, Formatter.class);
			return defaultformatter; //no register
		}
		return (FormatterBoolean) Home.getDefinitionSpace().resolve(FMT_BOOLEAN_DEFAULT);
	}

	private FormatterNumber obtainFormatterNumber() {
		if (!Home.getDefinitionSpace().containsKey(FMT_NUMBER_DEFAULT)) {
			final FormatterNumber defaultformatter = new FormatterNumber(FMT_NUMBER_DEFAULT);
			defaultformatter.initParameters("#,###.##");
			//Home.getDefinitionSpace().put(defaultformatter, Formatter.class);
			return defaultformatter; //no register
		}
		return (FormatterNumber) Home.getDefinitionSpace().resolve(FMT_NUMBER_DEFAULT);
	}

	private FormatterDate obtainFormatterDate() {
		if (!Home.getDefinitionSpace().containsKey(FMT_DATE_DEFAULT)) {
			final FormatterDate defaultformatter = new FormatterDate(FMT_DATE_DEFAULT);
			defaultformatter.initParameters("dd/MM/yyyy HH:mm ; dd/MM/yyyy");
			//Home.getDefinitionSpace().put(defaultformatter, Formatter.class);
			return defaultformatter; //no register
		}
		return (FormatterDate) Home.getDefinitionSpace().resolve(FMT_DATE_DEFAULT);
	}

	private FormatterString obtainFormatterString() {
		if (!Home.getDefinitionSpace().containsKey(FMT_STRING_DEFAULT)) {
			final FormatterString defaultformatter = new FormatterString(FMT_STRING_DEFAULT);
			defaultformatter.initParameters(null);//Fonctionnement de base (pas de formatage)
			//Home.getDefinitionSpace().put(defaultformatter, Formatter.class);
			return defaultformatter; //no register
		}
		return (FormatterString) Home.getDefinitionSpace().resolve(FMT_STRING_DEFAULT);
	}

}
