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
package vertigoimpl.engines.rest.grizzly.kraft;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

import vertigo.kernel.exception.VRuntimeException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

abstract class AbstractServices {

	final String process(final String name, final Map<String, ?> context) {
		try {
			final StringWriter writer = new StringWriter();
			final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
			final Reader reader = new InputStreamReader(this.getClass().getResourceAsStream(name + ".mustache"));
			try {
				final Mustache mustache = mustacheFactory.compile(reader, name);
				mustache.execute(writer, context);
			} finally {
				reader.close();
			}
			return writer.toString();
		} catch (final Exception e) {
			throw new VRuntimeException(e);
		}
	}
}
