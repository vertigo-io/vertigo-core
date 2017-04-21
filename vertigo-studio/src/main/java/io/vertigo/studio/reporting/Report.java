/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * Résultat d'une analyse.
 *
 * @author tchassagnette
 */
public final class Report {
	private final String name;
	private final List<ReportLine> dataReports;

	/**
	 * Constructeur.
	 */
	public Report(final String name, final List<ReportLine> dataReports) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dataReports);
		//-----
		this.name = name;
		this.dataReports = Collections.unmodifiableList(new ArrayList<>(dataReports));
	}

	public String getName() {
		return name;
	}

	/**
	 * @return Liste des rapports relatifs aux données.
	 */
	public List<ReportLine> getDataReports() {
		return dataReports;
	}
}
