/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.core.lang.Assertion;
import io.vertigo.studio.impl.reporting.renderer.DataReportRenderer;
import io.vertigo.studio.impl.reporting.renderer.ReportRenderer;
import io.vertigo.studio.reporting.DataReport;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.Report;

/**
 * @author tchassagnette
 *
 */
public final class ReportRendererImpl implements ReportRenderer {
	private final String rootPath;
	private final DataReportRenderer dataReportRenderer;

	/**
	 * Constructeur.
	 */
	public ReportRendererImpl(final String rootPath) {
		Assertion.checkArgNotEmpty(rootPath);
		//---------------------------------------------------------------------
		this.rootPath = rootPath;
		dataReportRenderer = new DataReportRendererImpl(rootPath);
	}

	public void render(final Report report) {
		Assertion.checkNotNull(report);
		//---------------------------------------------------------------------
		renderAllPages(report);
		//On crée la page d'index
		renderIndexPage(report);
	}

	private void renderAllPages(final Report report) {
		//On crée toutes les pages
		for (final DataReport dataReport : report.getDataReports()) {
			dataReportRenderer.render(dataReport);
		}
	}

	private void renderIndexPage(final Report report) {
		final StringBuilder sb = new StringBuilder();
		//---------------
		startRender(report, sb);
		//---------------
		//Création de la table	
		sb.append("<h1>Rapport pour les " + report.getDataReports().size() + " requêtes</h1>");
		sb.append("<table id=\"myTable\" class=\"tablesorter\">");
		//---------------
		boolean firstColumn = true;
		for (final DataReport dataReport : report.getDataReports()) {
			if (firstColumn) {
				sb.append(getTHead(dataReport));
				//On ouvre le corps de la table
				sb.append("<tbody>");
			}

			sb.append("<tr>");
			initItemRenderer(dataReport, sb);
			for (final Metric metric : dataReport.getMetrics()) {
				renderMetricData(metric, sb);
			}
			sb.append("</tr>");
			firstColumn = false;
		}
		sb.append("</tbody>");
		//---------------
		sb.append("</table>");
		//---------------
		endRender(sb);

		//On génère le fichier
		FileRendererUtil.writeFile(rootPath, "index.html", sb.toString());
	}

	private String getTHead(final DataReport dataReport) {
		final StringBuilder colSb = new StringBuilder();
		colSb.append("<thead><tr><th>");
		colSb.append("Tâche");
		colSb.append("</th>");
		for (final Metric metric : dataReport.getMetrics()) {
			colSb.append("<th>");
			colSb.append(metric.getTitle());
			colSb.append("</th>");
		}
		colSb.append("</tr>");
		colSb.append("</thead>");
		return colSb.toString();
	}

	private static void initItemRenderer(final DataReport dataReport, final StringBuilder stringBuilder) {
		stringBuilder.append("<td>");
		stringBuilder.append("<a href=\"" + dataReport.getFileName() + "\">");
		stringBuilder.append(dataReport.getTitle());
		stringBuilder.append("</a>");
		stringBuilder.append("</td>");
	}

	//Une métrique correspond à une cellule.
	private static void renderMetricData(final Metric metric, final StringBuilder stringBuilder) {
		stringBuilder.append("<td>");
		switch (metric.getStatus()) {
			case Rejected:
				stringBuilder.append("<font color='DodgerBlue'>--</font>");
				break;
			case Error:
				stringBuilder.append("<font color='Crimson'>");
				if (metric.getValue() == null) {
					stringBuilder.append("xxx");
				} else {
					stringBuilder.append(metric.getValue());
				}
				stringBuilder.append("</font>");
				break;
			case Executed:
				stringBuilder.append(metric.getValue());
				break;
			default:
				throw new IllegalArgumentException("case " + metric.getStatus() + " not implemented");
		}
		stringBuilder.append("</td>");
	}

	private static void startRender(final Report report, final StringBuilder sb) {
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<link rel=\"stylesheet\" href=\"..\\css\\tablesorter.css\" type=\"text/css\" media=\"print, projection, screen\" />");
		sb.append("<script type=\"text/javascript\" src=\"..\\js\\jquery-1.5.min.js\"></script> ");
		sb.append("<script type=\"text/javascript\" src=\"..\\js\\jquery.tablesorter.min.js\"></script> ");
		sb.append("<script type=\"text/javascript\">");
		sb.append("$(document).ready(function() ");
		sb.append(" { ");
		sb.append("     // add parser through the tablesorter addParser method");
		sb.append("     $.tablesorter.addParser({ ");
		sb.append("         // set a unique id");
		sb.append("         id: 'numericWithErrors',");
		sb.append("         is: function(s) { ");
		sb.append("              // return false so this parser is not auto detected");
		sb.append("              return false;");
		sb.append("          }, ");
		sb.append("          format: function(s) {");
		sb.append("              // format your data for normalization ");
		sb.append("             return s.replace(/--/,-1).replace(/xxx/,-2);");
		sb.append("          }, ");
		sb.append("          // set type, either numeric or text");
		sb.append("         type: 'numeric'");
		sb.append("     }); ");
		sb.append("     $(\"#myTable\").tablesorter({sortList: [[1,1]], headers: {1: {sorter:'numericWithErrors'}, 3: {sorter:'numericWithErrors'}});");
		sb.append("  } ");
		sb.append("); ");
		sb.append("</script>");
		sb.append("</head>");
		sb.append("<body>");
	}

	private static void endRender(final StringBuilder sb) {
		sb.append("</body>");
		sb.append("</html>");
	}

}
