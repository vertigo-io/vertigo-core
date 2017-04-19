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
import io.vertigo.studio.impl.reporting.renderer.ReportRenderer;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportLine;
import io.vertigo.studio.reporting.ReportMetric;

/**
 * @author tchassagnette
 *
 */
public final class ReportRendererImpl implements ReportRenderer {
	private final String rootPath;
	private final ReportLineRenderer dataReportRenderer;

	/**
	 * Constructeur.
	 */
	public ReportRendererImpl(final String rootPath) {
		Assertion.checkArgNotEmpty(rootPath);
		//-----
		this.rootPath = rootPath;
		dataReportRenderer = new ReportLineRendererImpl(rootPath);
	}

	@Override
	public void render(final Report report) {
		Assertion.checkNotNull(report);
		//-----
		renderAllPages(report);
		//On crée la page d'index
		renderIndexPage(report);
	}

	private void renderAllPages(final Report report) {
		//On crée toutes les pages
		for (final ReportLine dataReport : report.getDataReports()) {
			dataReportRenderer.render(dataReport);
		}
	}

	private void renderIndexPage(final Report report) {
		final StringBuilder sb = new StringBuilder();
		//-----
		startRender(report, sb);
		//-----
		//Création de la table
		sb.append("<h1>Rapport pour les ").append(report.getDataReports().size()).append(" requêtes</h1>");
		sb.append("<table id=\"myTable\" class=\"tablesorter\">");
		//-----
		boolean firstColumn = true;
		for (final ReportLine dataReport : report.getDataReports()) {
			if (firstColumn) {
				sb.append(getTHead(dataReport));
				//On ouvre le corps de la table
				sb.append("<tbody>");
			}

			sb.append("<tr>");
			initItemRenderer(dataReport, sb);
			for (final ReportMetric metric : dataReport.getMetrics()) {
				renderMetricData(metric, sb);
			}
			sb.append("</tr>");
			firstColumn = false;
		}
		sb.append("</tbody>");
		//-----
		sb.append("</table>");
		//-----
		endRender(sb);

		//On génère le fichier
		FileRendererUtil.writeFile(rootPath, "index.html", sb.toString());
	}

	private static String getTHead(final ReportLine dataReport) {
		final StringBuilder colSb = new StringBuilder()
				.append("<thead><tr><th>")
				.append("Tâche")
				.append("</th>");
		for (final ReportMetric metric : dataReport.getMetrics()) {
			colSb.append("<th>").append(metric.getTitle()).append("</th>");
		}
		return colSb
				.append("</tr>")
				.append("</thead>")
				.toString();
	}

	private static void initItemRenderer(final ReportLine dataReport, final StringBuilder stringBuilder) {
		stringBuilder
				.append("<td>")
				.append("<a href=\"").append(dataReport.getFileName()).append("\">")
				.append(dataReport.getTitle())
				.append("</a>")
				.append("</td>");
	}

	//Une métrique correspond à une cellule.
	private static void renderMetricData(final ReportMetric metric, final StringBuilder stringBuilder) {
		stringBuilder.append("<td>");
		switch (metric.getStatus()) {
			case REJECTETD:
				stringBuilder.append("<font color='DodgerBlue'>--</font>");
				break;
			case ERROR:
				stringBuilder.append("<font color='Crimson'>");
				if (metric.getValue() == null) {
					stringBuilder.append("xxx");
				} else {
					stringBuilder.append(metric.getValue());
				}
				stringBuilder.append("</font>");
				break;
			case EXECUTED:
				stringBuilder.append(metric.getValue());
				break;
			default:
				throw new IllegalArgumentException("case " + metric.getStatus() + " not implemented");
		}
		stringBuilder.append("</td>");
	}

	private static void startRender(final Report report, final StringBuilder sb) {
		sb
				.append("<html>")
				.append("<head>")
				.append("<link rel=\"stylesheet\" href=\"..\\css\\tablesorter.css\" type=\"text/css\" media=\"print, projection, screen\" />")
				.append("<script type=\"text/javascript\" src=\"..\\js\\jquery-1.5.min.js\"></script> ")
				.append("<script type=\"text/javascript\" src=\"..\\js\\jquery.tablesorter.min.js\"></script> ")
				.append("<script type=\"text/javascript\">")
				.append("$(document).ready(function() ")
				.append(" { ")
				.append("     // add parser through the tablesorter addParser method")
				.append("     $.tablesorter.addParser({ ")
				.append("         // set a unique id")
				.append("         id: 'numericWithErrors',")
				.append("         is: function(s) { ")
				.append("              // return false so this parser is not auto detected")
				.append("              return false;")
				.append("          }, ")
				.append("          format: function(s) {")
				.append("              // format your data for normalization ")
				.append("             return s.replace(/--/,-1).replace(/xxx/,-2);")
				.append("          }, ")
				.append("          // set type, either numeric or text")
				.append("         type: 'numeric'")
				.append("     }); ")
				.append("     $(\"#myTable\").tablesorter({sortList: [[1,1]], headers: {1: {sorter:'numericWithErrors'}, 3: {sorter:'numericWithErrors'}});")
				.append("  } ")
				.append("); ")
				.append("</script>")
				.append("</head>")
				.append("<body>");
	}

	private static void endRender(final StringBuilder sb) {
		sb.append("</body>");
		sb.append("</html>");
	}

}
