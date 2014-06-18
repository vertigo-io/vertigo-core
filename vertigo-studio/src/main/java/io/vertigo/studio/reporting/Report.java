package io.vertigo.studio.reporting;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Résultat d'une analyse.
 * 
 * @author tchassagnette
 */
public final class Report {
	private final List<DataReport> dataReports;

	/**
	 * Constructeur.
	 */
	public Report(final List<DataReport> dataReports) {
		Assertion.checkNotNull(dataReports);
		//---------------------------------------------------------------------
		this.dataReports = Collections.unmodifiableList(new ArrayList<>(dataReports));
	}

	/**
	 * @return Liste des rapports relatifs aux données. 
	 */
	public List<DataReport> getDataReports() {
		return dataReports;
	}
}
