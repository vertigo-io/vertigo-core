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
package io.vertigo.commons.plugins.analytics.analytica.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;

/**
 * Builder permettant de contruire un processus.
 * Il y a deux modes de creation.
 *  - live (La date de debut et celle de la creation , la duree s'obtient lors de la creation du process
 *  - differe (la date de debute et la duree sont renseignee ensembles )
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcessBuilder.java,v 1.18 2012/11/08 17:06:27 pchretien Exp $
 */
public final class AProcessBuilder {
	private final String appName;
	private final String myType;
	private final Date startDate;

	private String myLocation;
	private String myCategory;

	//Tableau des mesures identifiees par leur nom.
	private final Map<String, Double> measures;

	//Tableau des metadonnees identifiees par leur nom.
	private final Map<String, String> metaDatas;

	private final long start;
	private Double durationMs = null;
	private final List<AProcess> subProcesses;
	private final AProcessBuilder parent;

	/**
	 * Constructeur.
	 * La duree du processus sera obtenue lors de l'appel a la methode build().
	 * @param type Type du processus
	 */
	public AProcessBuilder(final String appName, final String type) {
		this(appName, type, null, new Date(), null);
	}

	/**
	 * Constructeur .
	 * @param type Type du processus
	 * @param startDate Date de debut processus
	 * @param durationMs Duree du processus (Millisecondes)
	 */
	public AProcessBuilder(final String appName, final String type, final Date startDate, final double durationMs) {
		this(appName, type, null, startDate, durationMs);
	}

	private AProcessBuilder(final String appName, final String type, final AProcessBuilder parent, final Date startDate, final Double durationMs) {
		Assertion.checkNotNull(appName, "appName is required");
		Assertion.checkNotNull(type, "type of process is required");
		Assertion.checkNotNull(startDate, "start of process is required");
		//---
		this.appName = appName;
		myType = type;

		measures = new HashMap<>();
		metaDatas = new HashMap<>();
		subProcesses = new ArrayList<>();
		this.startDate = startDate;
		start = startDate.getTime();
		this.parent = parent;
		//---------------------------------------------------------------------
		this.durationMs = durationMs;
	}

	public AProcessBuilder withLocation(final String location) {
		myLocation = location;
		return this;
	}

	public AProcessBuilder withLocation(final String... locations) {
		myLocation = Arrays.stream(locations)
				.collect(Collectors.joining(AProcess.LOCATION_SEPARATOR));
		return this;
	}

	public AProcessBuilder withCategory(final String category) {
		myCategory = category;
		return this;
	}

	public AProcessBuilder withCategory(final String... categories) {
		myCategory = Arrays.stream(categories)
				.collect(Collectors.joining(AProcess.CATEGORY_SEPARATOR));
		return this;
	}

	/**
	 * Increment d'une mesure.
	 * Si la mesure est nouvelle, elle est automatiquement creee avec la valeur
	 * @param mName Nom de la mesure
	 * @param mValue  Valeur a incrementer
	 * @return Builder
	 */
	public AProcessBuilder incMeasure(final String mName, final double mValue) {
		Assertion.checkNotNull(mName, "Measure name is required");
		//---------------------------------------------------------------------
		final Double lastmValue = measures.get(mName);
		measures.put(mName, lastmValue == null ? mValue : mValue + lastmValue);
		return this;
	}

	/**
	 * Mise a jour d'une mesure.
	 * @param mName Nom de la mesure
	 * @param mValue  Valeur é incrémenter
	 * @return Builder
	 */
	public AProcessBuilder setMeasure(final String mName, final double mValue) {
		Assertion.checkNotNull(mName, "Measure name is required");
		//---------------------------------------------------------------------
		measures.put(mName, mValue);
		return this;
	}

	/**
	 * Mise a jour d'une metadonnee.
	 * @param mmetaDataName Nom de la metadonnee
	 * @param mmetaDataValue  Valeur de la metadonnee
	 * @return Builder
	 */
	public AProcessBuilder addMetaData(final String mmetaDataName, final String mmetaDataValue) {
		Assertion.checkNotNull(mmetaDataName, "Metadata name is required");
		Assertion.checkNotNull(mmetaDataValue, "Metadata value is required");
		//---------------------------------------------------------------------
		metaDatas.put(mmetaDataName, mmetaDataValue);
		return this;
	}

	/**
	 * Mise a jour d'une metadonnee.
	 * @param mdName Nom de la metadonnee
	 * @param mdValues Valeurs de la metadonnee
	 * @return Builder
	 */
	public AProcessBuilder addMetaData(final String mdName, final Set<String> mdValues) {
		Assertion.checkNotNull(mdName, "Metadata name is required");
		Assertion.checkNotNull(mdValues, "Metadata value is required");
		//---------------------------------------------------------------------
		final StringBuilder metadataBuilder = new StringBuilder();
		final Iterator<String> mdValuesIterator = mdValues.iterator();
		while (mdValuesIterator.hasNext()) {
			metadataBuilder.append(mdValuesIterator.next()).append("/");
		}

		metaDatas.put(mdName, metadataBuilder.toString());
		return this;
	}

	/**
	 * Ajout d'un sous processus.
	 * @param subStartDate Date de debut
	 * @param subDurationMs Duree du sous process en Ms
	 * @param type Type du sous process
	 * @return Builder
	 */
	public AProcessBuilder beginSubProcess(final String type, final Date subStartDate, final double subDurationMs) {
		return new AProcessBuilder(appName, type, this, subStartDate, subDurationMs)
				.withLocation(myLocation);
	}

	/**
	 * Fin d'un sous processus.
	 * Le sous processus est automatiquement ajoute au processus parent.
	 * @return Builder
	 */
	public AProcessBuilder endSubProcess() {
		Assertion.checkNotNull(parent, "parent is required when you close a subprocess");
		//---------------------------------------------------------------------
		parent.addSubProcess(build());
		return parent;
	}

	/**
	 * Ajout d'un sous processus.
	 * @param subProcess Sous-Processus a ajouter
	 * @return Builder
	 */
	public AProcessBuilder addSubProcess(final AProcess subProcess) {
		return addSubProcess(subProcess, true);
	}

	private AProcessBuilder addSubProcess(final AProcess subProcess, final boolean addSubDuration) {
		Assertion.checkNotNull(subProcess, "sub process is required ");
		//---------------------------------------------------------------------
		subProcesses.add(subProcess);
		if (addSubDuration) {
			incMeasure(AProcess.SUB_DURATION, subProcess.getDuration());
		}
		return this;
	}

	/**
	 * Construction du Processus.
	 * @return Process
	 */
	public AProcess build() {
		//Si on est dans le mode de construction en runtime, on ajoute la duree.
		if (durationMs == null) {
			durationMs = Long.valueOf(System.currentTimeMillis() - start).doubleValue();
		}
		//On ajoute la mesure obligatoire : duree
		setMeasure(AProcess.DURATION, durationMs);
		return new AProcess(
				appName,
				myType,
				myCategory,
				myLocation,
				startDate,
				measures,
				metaDatas,
				subProcesses);
	}
}
