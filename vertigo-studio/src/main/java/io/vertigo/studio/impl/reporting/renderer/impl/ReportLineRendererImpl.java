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
package io.vertigo.studio.impl.reporting.renderer.impl;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.reporting.renderer.ReportLineRenderer;
import io.vertigo.studio.reporting.ReportLine;
import io.vertigo.studio.reporting.ReportMetric;

/**
 * @author pchretien
 */
final class ReportLineRendererImpl implements ReportLineRenderer {
	private final String rootPath;

	ReportLineRendererImpl(final String rootPath) {
		Assertion.checkArgNotEmpty(rootPath);
		//-----
		this.rootPath = rootPath;
	}

	@Override
	public void render(final ReportLine dataReport) {
		Assertion.checkNotNull(dataReport);
		//-----
		final StringBuilder sb = new StringBuilder();

		//-----
		startRender(sb);
		//-----
		sb.append(dataReport.getHtmlDescription());
		//-----
		for (final ReportMetric metric : dataReport.getMetrics()) {
			renderMetric(sb, metric);
		}
		//-----
		endRender(sb);
		//-----
		FileRendererUtil.writeFile(rootPath, dataReport.getFileName(), sb.toString());
	}

	private static void renderMetric(final StringBuilder sb, final ReportMetric metric) {
		sb.append(metric.getTitle()).append(" : ");
		String valueInformation = metric.getValueInformation();
		if (valueInformation != null) {
			valueInformation = valueInformation.replaceAll("\n", "<br/>");
			sb.append(valueInformation);
		} else {
			sb.append(metric.getValue()).append(' ').append(metric.getUnit());
		}
		sb.append("<hr />");
	}

	private static void startRender(final StringBuilder sb) {
		sb
				.append("<html>")
				.append("<head>")
				.append("</head>")
				.append("<body>");
	}

	private static void endRender(final StringBuilder sb) {
		sb
				.append("</body>")
				.append("</html>");
	}

}
