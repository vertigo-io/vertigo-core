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
package io.vertigo.dynamox.domain.constraint;

import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractConstraintImpl;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

/**
 * Contrainte sur la valeur maximale d'un nombre.
 * arguments = valeur maximale.
 * @author npiedeloup
 */
public final class ConstraintNumberMaximum extends AbstractConstraintImpl<Number, Number> {
	private double maxValue;

	public ConstraintNumberMaximum(final String urn) {
		super(urn);
	}

	/**{@inheritDoc}*/
	@Override
	public void initParameters(final String args) {
		Assertion.checkArgument(args != null && args.length() > 0, "Vous devez préciser la valeur maximum comme argument de ConstraintNumberMaximum");
		//---------------------------------------------------------------------
		maxValue = Double.valueOf(args);
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final Number value) {
		if (value == null) {
			return true;
		}
		return value.doubleValue() <= maxValue;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_NUMBER_MAXIMUM, maxValue);
	}

	/** {@inheritDoc} */
	public Property getProperty() {
		return DtProperty.MAX_VALUE;
	}

	/** {@inheritDoc} */
	public Number getPropertyValue() {
		return maxValue;
	}
}
