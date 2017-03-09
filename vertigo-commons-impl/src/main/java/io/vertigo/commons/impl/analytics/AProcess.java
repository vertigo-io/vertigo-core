/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.vertigo.lang.Assertion;

/**
 * Un processus est un evenement
 *   - declenche dans une application specifique
 *   - relatif a un type d'evenement (exemple : metrics des pages, des requetes sql, des mails envoyés, des services ...)
 *
 *	Un evenement est defini selon 3 axes
 *	 - when, quand a eu lieu l'evenement
 *	 - what, de quoi s'agit-il ?
 *	 - where, ou s'est passe l'evenement  ? sur quel serveur ?
 *
 *	[what]
 * - category (examples : sql, jpa, tasks)
 * - name (examples : /create/movies)

 * 	[when]
 * - start timestamp
 * - end   timestamp
 *
 * 	[data]
 * - list of measures
 * - list of tagss
 * - list of sub processes (0..*)
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcess.java,v 1.8 2012/10/16 17:18:26 pchretien Exp $
 */
public final class AProcess {
	/**
	 * REGEX used to define rules on category, mesaures and tags.
	 */
	public static final Pattern PROCESS_CATEGORY_REGEX = Pattern.compile("[a-z]+");
	public static final Pattern MEASURE_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]+");
	public static final Pattern TAG_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]+");

	public static final String CATEGORY_SEPARATOR = "/";
	private final String category; //ex : sql, page....

	private final String name; //what ex : accounts/search

	private final long start; //when
	private final long end; //when

	private final Map<String, Double> measures;
	private final Map<String, String> tags;
	private final List<AProcess> subProcesses;

	/**
	 * Constructor.
	 * @param category the category
	 * @param name  the name
	 * @param start the start instant
	 * @param end the end instant
	 * @param measures the measures
	 * @param tags the tags
	 * @param subProcesses the list of sub processes (0..*)
	 */
	AProcess(
			final String category,
			final String name,
			final Instant start,
			final Instant end,
			final Map<String, Double> measures,
			final Map<String, String> tags,
			final List<AProcess> subProcesses) {
		Assertion.checkNotNull(category, "the category of the process is required");
		Assertion.checkNotNull(name, "the name of the process is required");
		Assertion.checkNotNull(start, "the start is required");
		Assertion.checkNotNull(end, "the end is required");
		Assertion.checkNotNull(measures, "the measures are required");
		Assertion.checkNotNull(tags, "the tags are required");
		Assertion.checkNotNull(subProcesses, "the subProcesses are required");

		//---
		checkRegex(category, PROCESS_CATEGORY_REGEX, "process type");
		//	ckeckRegex(type, CATEGORY_REGEX, "category");
		measures.keySet()
				.forEach(measureName -> checkRegex(measureName, MEASURE_REGEX, "measure name"));
		tags.keySet()
				.forEach(tagName -> checkRegex(tagName, TAG_REGEX, "metadata name"));
		//---------------------------------------------------------------------
		this.category = category;
		this.name = name;
		this.start = start.toEpochMilli();
		this.end = end.toEpochMilli();
		this.measures = Collections.unmodifiableMap(new HashMap<>(measures));
		this.tags = Collections.unmodifiableMap(new HashMap<>(tags));
		this.subProcesses = subProcesses;
	}

	private static void checkRegex(final String s, final Pattern pattern, final String info) {
		if (!pattern.matcher(s).matches()) {
			throw new IllegalArgumentException(info + " " + s + " must match regex :" + pattern.pattern());
		}
	}

	/**
	 * [what]
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return the duration of the process (in milliseconds)
	 */
	public long getDurationMillis() {
		return end - start;
	}

	/**
	 * [when]
	 * @return the start timestamp in Millis
	 */
	public long getStart() {
		return start;
	}

	/**
	 * [when]
	 * @return the end timestamp in Millis
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * @return the measures of the process
	 */
	public Map<String, Double> getMeasures() {
		return measures;
	}

	/**
	 * @return the tags of the process
	 */
	public Map<String, String> getTags() {
		return tags;
	}

	/**
	 * @return the list of sub processes
	 */
	public List<AProcess> getSubProcesses() {
		return subProcesses;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "{category :" + category + ", name :" + name + "}";
	}
}
