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
package io.vertigo.struts2.impl.views.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.StrutsClassTemplateLoader;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

/**
 *
 * Vertigo FreemarkerManager to override ftl loading order.
 * 1/ look into webapp directory
 *     You can also override this behaviour with a context parameter in web.xml :
 *     The parameter's name is : TemplatePath
 *     The parameter is multivalued : Separator is ";"
 *     Possible prefixes :
 *     		webapp://<prefixdir> : Add a TemplateLoader using the webapp loader specifying the prefix : empty means "/"
 *     		class://<prefixdir> : Add a TemplateLoader using the classpath specifying the prefix : empty means "/"
 *     		file://<absolutePath> : Add a loader using URL
 *     		struts2:// : The default struts2 templateLoader
 *     Example : webapp://WEB-INF/classes/
 *
 * 2/ look into /io/vertigo/struts2/ftl/ directory in classPath (ftl override in Vertigo jar)
 * 3/ look into / directory in classPath (default to Struts2 jar)
 *
 * Use of this VFreeMarkerManager : add this to your struts.xml
 *       <constant name="struts.freemarker.manager.classname" value="io.vertigo.struts2.impl.views.freemarker.VFreemarkerManager" />
 *
 */
public final class VFreemarkerManager extends FreemarkerManager {
	private static String SEP = ";";
	private static final String DEFAUT_TEMPLATES_PATH = "webapp://"; //presume that most apps will require the webapp and class template loader
	private static final String IMPLICIT_TEMPLATES_PATH = "class://io/vertigo/struts2/ftl;struts2://"; //struts2:// => default to struts2

	/** {@inheritDoc} */
	@Override
	protected TemplateLoader createTemplateLoader(final ServletContext servletContext, final String templatesPath) {
		final List<TemplateLoader> templatesLoaders = new ArrayList<>();
		final String usedTemplatesPath;
		if (templatesPath != null) {
			usedTemplatesPath = templatesPath + SEP + IMPLICIT_TEMPLATES_PATH;
		} else {
			usedTemplatesPath = DEFAUT_TEMPLATES_PATH + SEP + IMPLICIT_TEMPLATES_PATH;
		}
		for (final String singleTemplatePath : usedTemplatesPath.split(SEP)) {
			try {
				final TemplateLoader templatePathLoader;
				if (singleTemplatePath.startsWith("class://")) {
					// substring(length-1) is intentional as we "reuse" the last slash
					templatePathLoader = new ClassTemplateLoader(getClass(),
							singleTemplatePath.substring("class://".length() - 1));
				} else if (singleTemplatePath.startsWith("file://")) {
					templatePathLoader = new FileTemplateLoader(new File(singleTemplatePath.substring("file://".length())));
				} else if (singleTemplatePath.startsWith("webapp://")) {
					// substring(length-1) is intentional as we "reuse" the last slash
					templatePathLoader = new WebappTemplateLoader(servletContext, singleTemplatePath.substring("webapp://"
							.length() - 1));
				} else if ("struts2://".equals(singleTemplatePath)) {
					// substring(length-1) is intentional as we "reuse" the last slash
					templatePathLoader = new StrutsClassTemplateLoader();
				} else {
					throw new IllegalArgumentException(
							"templatePath '"
									+ singleTemplatePath
									+ "' invalid prefix, use 'class://<prefixpackage>', 'file://<absolutefile>' or 'webapp://<prefixdir>'");
				}
				templatesLoaders.add(templatePathLoader);
			} catch (final IOException e) {
				throw new IllegalArgumentException(
						"templatePath '"
								+ singleTemplatePath
								+ "' invalid. Use "
								+ SEP
								+ " as separator and a valid prefix: 'class://<prefixpackage>', 'file://<absolutefile>' or 'webapp://<prefixdir>'",
						e);
			}
		}
		return new MultiTemplateLoader(templatesLoaders.toArray(new TemplateLoader[templatesLoaders.size()]));
	}
}
