package io.vertigo.studio.impl.reporting;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.impl.reporting.renderer.ReportRenderer;
import io.vertigo.studio.impl.reporting.renderer.impl.ReportRendererImpl;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportingManager;
import io.vertigo.studio.reporting.ReportingPlugin;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation de ReportingManager.
 * 
 * @author tchassagnette
 * @version $Id: ReportingManagerImpl.java,v 1.4 2013/10/22 10:47:33 pchretien Exp $
 */
public final class ReportingManagerImpl implements ReportingManager {
	private final String rootPath;
	@Inject
	private List<ReportingPlugin> reportingPlugins;

	/**
	 * @param rootPath Racine de stockage des raports
	 */
	@Inject
	public ReportingManagerImpl(@Named("rootPath") final String rootPath) {
		Assertion.checkArgNotEmpty(rootPath);
		Assertion.checkArgument(rootPath.endsWith("/"), "le path doit se terminer par un /");
		//---------------------------------------------------------------------
		this.rootPath = rootPath;
	}

	/** {@inheritDoc} */
	public void analyze() {
		for (final ReportingPlugin reportingPlugin : getReportingPlugins()) {
			final Report report = reportingPlugin.analyze();
			final ReportRenderer reportRenderer = new ReportRendererImpl(rootPath + reportingPlugin.getClass().getSimpleName() + "/");
			reportRenderer.render(report);
		}
	}

	/** {@inheritDoc} */
	public List<ReportingPlugin> getReportingPlugins() {
		return Collections.unmodifiableList(reportingPlugins);
	}
}
