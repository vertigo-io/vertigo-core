/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiére - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.vertigo.commons.impl.analytics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This builder allows  to build a process in a fluent way.
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcessBuilder.java,v 1.18 2012/11/08 17:06:27 pchretien Exp $
 */
final class AProcessBuilder implements Builder<AProcess> {
	private final String myCategory;
	private final Instant start;

	private final String myName;

	private final Map<String, Double> measures;
	private final Map<String, String> tags;

	private final List<AProcess> subProcesses;

	/**
	 * Constructeur.
	 * La duree du processus sera obtenue lors de l'appel a la methode build().
	 * @param type Type du processus
	 */
	AProcessBuilder(final String category, final String name) {
		Assertion.checkArgNotEmpty(category, "the process category is required");
		Assertion.checkArgNotEmpty(name, "the process name is required");
		//---
		myCategory = category;
		myName = name;

		measures = new HashMap<>();
		tags = new HashMap<>();
		subProcesses = new ArrayList<>();
		this.start = Instant.now();
	}

	/**
	 * Increments a measure.
	 * if the measure is new,  it's automatically created with the value.
	 * @param name the measure name
	 * @param value  the measure value to increment
	 * @return this builder
	 */
	AProcessBuilder incMeasure(final String measureName, final double measureValue) {
		Assertion.checkNotNull(measureName, "Measure name is required");
		//---------------------------------------------------------------------
		final Double lastmValue = measures.get(measureName);
		measures.put(measureName, lastmValue == null ? measureValue : measureValue + lastmValue);
		return this;
	}

	/**
	 * Upserts a mesaure defined by a name and a value.
	 * @param name  the measure name
	 * @param value  the value measure
	 * @return this builder
	 */
	AProcessBuilder setMeasure(final String name, final double value) {
		Assertion.checkNotNull(name, "measure name is required");
		//---------------------------------------------------------------------
		measures.put(name, value);
		return this;
	}

	/**
	 * Adds a tag defined by a name and a value.
	 * @param name the tag name
	 * @param value  the tag value
	 * @return this builder
	 */
	AProcessBuilder addTag(final String name, final String value) {
		Assertion.checkNotNull(name, "tag name is required");
		Assertion.checkNotNull(value, "tag value is required");
		//---------------------------------------------------------------------
		tags.put(name, value);
		return this;
	}

	/**
	 * adds a sub process d'un sous processus.
	 * @param subProcess the sub process to add
	 * @return this builder
	 */
	AProcessBuilder addSubProcess(final AProcess subProcess) {
		Assertion.checkNotNull(subProcess, "sub process is required ");
		//---------------------------------------------------------------------
		subProcesses.add(subProcess);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AProcess build() {
		final Instant end = Instant.now();
		return new AProcess(
				myCategory,
				myName,
				start,
				end,
				measures,
				tags,
				subProcesses);
	}
}
