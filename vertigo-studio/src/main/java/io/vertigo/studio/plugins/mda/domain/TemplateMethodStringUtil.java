package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.kernel.util.StringUtil;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Méthode Freemarker 'constToCamelCase'.
 * si : config.setSharedVariable("constToCamelCase", new TemplateMethodStringHelper());
 * Exemple : execute${constToCamelCase(action.name)?cap_first}()
 * TemplateMethodModel : les params sont considérés comme des String.
 * 
 * @author  dchallas
 */
public class TemplateMethodStringUtil implements TemplateMethodModel {

	/** {@inheritDoc}*/
	public TemplateModel exec(final List params) throws TemplateModelException {
		final String str = (String) params.get(0);
		return new SimpleScalar(StringUtil.constToCamelCase(str, false));

	}
}
