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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
final class AProcessBuilder {
	private final String myType;
	private final Instant start;

	private String myCategory;

	//Tableau des mesures identifiees par leur nom.
	private final Map<String, Double> measures;

	//Tableau des metadonnees identifiees par leur nom.
	private final Map<String, String> metaDatas;

	private final List<AProcess> subProcesses;

	/**
	 * Constructeur.
	 * La duree du processus sera obtenue lors de l'appel a la methode build().
	 * @param type Type du processus
	 */
	AProcessBuilder(final String type) {
		this(type, Instant.now());
	}

	private AProcessBuilder(final String type, final Instant start) {
		Assertion.checkNotNull(type, "type of process is required");
		Assertion.checkNotNull(start, "start of process is required");
		//---
		myType = type;

		measures = new HashMap<>();
		metaDatas = new HashMap<>();
		subProcesses = new ArrayList<>();
		this.start = start;
	}

	AProcessBuilder withCategory(final String category) {
		myCategory = category;
		return this;
	}

	AProcessBuilder withCategory(final String... categories) {
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
	AProcessBuilder incMeasure(final String measureName, final double measureValue) {
		Assertion.checkNotNull(measureName, "Measure name is required");
		//---------------------------------------------------------------------
		final Double lastmValue = measures.get(measureName);
		measures.put(measureName, lastmValue == null ? measureValue : measureValue + lastmValue);
		return this;
	}

	/**
	 * Mise a jour d'une mesure.
	 * @param mName Nom de la mesure
	 * @param mValue  Valeur é incrémenter
	 * @return Builder
	 */
	AProcessBuilder setMeasure(final String measureName, final double measureValue) {
		Assertion.checkNotNull(measureName, "Measure name is required");
		//---------------------------------------------------------------------
		measures.put(measureName, measureValue);
		return this;
	}

	/**
	 * Mise a jour d'une metadonnee.
	 * @param mmetaDataName Nom de la metadonnee
	 * @param mmetaDataValue  Valeur de la metadonnee
	 * @return Builder
	 */
	AProcessBuilder addMetaData(final String mmetaDataName, final String mmetaDataValue) {
		Assertion.checkNotNull(mmetaDataName, "Metadata name is required");
		Assertion.checkNotNull(mmetaDataValue, "Metadata value is required");
		//---------------------------------------------------------------------
		metaDatas.put(mmetaDataName, mmetaDataValue);
		return this;
	}

	/**
	 * Ajout d'un sous processus.
	 * @param subProcess Sous-Processus a ajouter
	 * @return Builder
	 */
	AProcessBuilder addSubProcess(final AProcess subProcess) {
		Assertion.checkNotNull(subProcess, "sub process is required ");
		//---------------------------------------------------------------------
		subProcesses.add(subProcess);
		return this;
	}

	/**
	 * Construction du Processus.
	 * @return Process
	 */
	public AProcess build() {
		final Instant end = Instant.now();
		return new AProcess(
				myType,
				myCategory,
				start,
				end,
				measures,
				metaDatas,
				subProcesses);
	}
}
