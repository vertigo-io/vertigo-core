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
package io.vertigo.studio.plugins.mda.domain.templates;

import io.vertigo.util.StringUtil;

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
	@Override
	public TemplateModel exec(final List params) throws TemplateModelException {
		final String str = (String) params.get(0);
		return new SimpleScalar(StringUtil.constToLowerCamelCase(str));
	}
}
