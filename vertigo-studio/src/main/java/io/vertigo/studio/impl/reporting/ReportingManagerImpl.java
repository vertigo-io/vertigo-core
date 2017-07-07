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
package io.vertigo.studio.impl.reporting;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.reporting.renderer.ReportRenderer;
import io.vertigo.studio.impl.reporting.renderer.impl.ReportRendererImpl;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportingManager;

/**
 * Implémentation de ReportingManager.
 *
 * @author pchretien
 */
public final class ReportingManagerImpl implements ReportingManager {
	private final String rootPath;
	private final List<ReportingPlugin> reportingPlugins;

	/**
	 * @param rootPath Racine de stockage des raports
	 * @param reportingPlugins reportingPlugins
	 */
	@Inject
	public ReportingManagerImpl(
			@Named("rootPath") final String rootPath,
			final List<ReportingPlugin> reportingPlugins) {
		Assertion.checkArgNotEmpty(rootPath);
		Assertion.checkArgument(rootPath.endsWith("/"), "le path doit se terminer par un /");
		Assertion.checkNotNull(reportingPlugins);
		//-----
		this.rootPath = rootPath;
		this.reportingPlugins = reportingPlugins;
	}

	/** {@inheritDoc} */
	@Override
	public List<Report> analyze() {
		return reportingPlugins
				.stream()
				.map(ReportingPlugin::analyze)
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public void render(final Report report) {
		final ReportRenderer reportRenderer = new ReportRendererImpl(rootPath + report.getName() + "/");
		reportRenderer.render(report);
	}

}
