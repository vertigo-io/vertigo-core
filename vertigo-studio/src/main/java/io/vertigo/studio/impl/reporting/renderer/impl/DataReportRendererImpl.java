package io.vertigo.studio.impl.reporting.renderer.impl;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.impl.reporting.renderer.DataReportRenderer;
import io.vertigo.studio.reporting.DataReport;
import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 */
final class DataReportRendererImpl implements DataReportRenderer {
	private final String rootPath;

	DataReportRendererImpl(final String rootPath) {
		Assertion.checkArgNotEmpty(rootPath);
		//---------------------------------------------------------------------
		this.rootPath = rootPath;
	}

	public void render(final DataReport dataReport) {
		Assertion.checkNotNull(dataReport);
		//---------------------------------------------------------------------
		final StringBuilder sb = new StringBuilder();

		//---------
		startRender(sb);
		//---------
		sb.append(dataReport.getHtmlDescription());
		//---------
		for (final Metric metric : dataReport.getMetrics()) {
			renderMetric(sb, metric);
		}
		//---------
		endRender(sb);
		//---------
		FileRendererUtil.writeFile(rootPath, dataReport.getFileName(), sb.toString());
	}

	private void renderMetric(final StringBuilder sb, final Metric metric) {
		sb.append(metric.getTitle()).append(" : ");
		String valueInformation = metric.getValueInformation();
		if (valueInformation != null) {
			valueInformation = valueInformation.replaceAll("\n", "<br/>");
			sb.append(valueInformation);
		} else {
			sb.append(metric.getValue()).append(" ").append(metric.getUnit());
		}
		sb.append("<hr />");
	}

	private void startRender(final StringBuilder sb) {
		sb.append("<html>");
		sb.append("<head>");
		sb.append("</head>");
		sb.append("<body>");
	}

	private void endRender(final StringBuilder sb) {
		sb.append("</body>");
		sb.append("</html>");
	}

}
