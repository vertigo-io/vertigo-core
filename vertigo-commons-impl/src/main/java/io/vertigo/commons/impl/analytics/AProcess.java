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
 * - categories defined an array of string : search/items

 * 	[when]
 * - a start date
 *
 * 	[data]
 * - a duration (cf.measures)
 * - a list of measures with a DURATION measure
 * - a list of metadatas
 *
 * - a list of sub processes (0..*)
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcess.java,v 1.8 2012/10/16 17:18:26 pchretien Exp $
 */
public final class AProcess {
	/**
	 * REGEX décrivant les régles sur les noms (type de process, mesures et metadata, . (exemples : sql, mail, services)
	 */
	public static final Pattern PROCESS_TYPE_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]+");
	public static final Pattern MEASURE_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]+");
	public static final Pattern METADATA_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]+");
	//	public static final Pattern CATEGORY_REGEX = Pattern.compile("[a-z][a-z//-_]*");

	public static final String CATEGORY_SEPARATOR = "/";
	public static final String LOCATION_SEPARATOR = "/";
	private final String type; //ex : sql, page....

	private final String category; //what ex : accounts/search

	private final Instant start; //when
	private final Instant end; //when

	private final Map<String, Double> measures;
	private final Map<String, String> metaDatas;
	private final List<AProcess> subProcesses;

	/**
	 * @param type the type of the proceee
	 * @param category  Category
	 * @param startDate Date du processus
	 * @param measures Mesures du processus
	 * @param metaDatas lis of the metadatas
	 * @param subProcesses list of the sub processes (0..*)
	 */
	AProcess(
			final String type,
			final String category,
			final Instant start,
			final Instant end,
			final Map<String, Double> measures,
			final Map<String, String> metaDatas,
			final List<AProcess> subProcesses) {
		Assertion.checkNotNull(type, "the type of the process is required");
		Assertion.checkNotNull(category, "the category of the process is required");
		Assertion.checkNotNull(start, "the start is required");
		Assertion.checkNotNull(end, "the end is required");
		Assertion.checkNotNull(measures, "the measures are required");
		Assertion.checkNotNull(metaDatas, "the metaDatas are required");
		Assertion.checkNotNull(subProcesses, "the subProcesses are required");

		//---
		checkRegex(type, PROCESS_TYPE_REGEX, "process type");
		//	ckeckRegex(type, CATEGORY_REGEX, "category");
		measures.keySet()
				.forEach(measureName -> checkRegex(measureName, MEASURE_REGEX, "metadata name"));
		metaDatas.keySet()
				.forEach(metaDataName -> checkRegex(metaDataName, METADATA_REGEX, "metadata name"));
		//---------------------------------------------------------------------
		this.type = type;
		this.category = category;
		this.start = start;
		this.end = end;
		this.measures = Collections.unmodifiableMap(new HashMap<>(measures));
		this.metaDatas = Collections.unmodifiableMap(new HashMap<>(metaDatas));
		this.subProcesses = subProcesses;
	}

	private static void checkRegex(final String s, final Pattern pattern, final String info) {
		if (!pattern.matcher(s).matches()) {
			throw new IllegalArgumentException(info + " " + s + " must match regex :" + pattern.pattern());
		}
	}

	/**
	 * @return Type du processus
	 */
	public String getType() {
		return type;
	}

	/**
	 * [what]
	 * @return Category
	 */
	public String getCategory() {
		return category;
	}

	public String[] getCategoryAsArray() {
		return category.split(CATEGORY_SEPARATOR);
	}

	/**
	 * @return Process duration */
	public long getDurationMilli() {
		return end.toEpochMilli() - start.toEpochMilli();
	}

	/**
	 * [when]
	 * @return Date processus
	 */
	public Instant getStart() {
		return start;
	}

	/**
	 * [when]
	 * @return Date processus
	 */
	public Instant getEnd() {
		return end;
	}

	/**
	 * @return Mesures du processus
	 */
	public Map<String, Double> getMeasures() {
		return measures;
	}

	/**
	 * @return Metadonnees du processus
	 */
	public Map<String, String> getMetaDatas() {
		return metaDatas;
	}

	/**
	 * @return Liste des sous-processus
	 */
	public List<AProcess> getSubProcesses() {
		return subProcesses;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "{type:" + type + ", category :" + category + "}";
	}
}
