/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.impl.security;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.ResourceNameFactory;
import io.vertigo.util.BeanUtil;

/**
 * ResourceNameFactory standard des beans securisees.
 * @author npiedeloup
 * @deprecated Use new account security management instead
 */
@Deprecated
public final class BeanResourceNameFactory implements ResourceNameFactory {
	private final String securityPattern;
	private final List<String> securityPatternTokenized = new ArrayList<>();

	/**
	 * Constructor.
	 * Prend en entrée le pattern de la chaine de resource à produire.
	 * Il peut être paramétré avec des propriétés de l'objet avec la syntaxe : ${maPropriete}
	 * @param securityPattern Pattern de la resource.
	 */
	public BeanResourceNameFactory(final String securityPattern) {
		io.vertigo.lang.Assertion.checkNotNull(securityPattern);
		//-----
		this.securityPattern = securityPattern;
		int previousIndex = 0;
		int nextIndex = securityPattern.indexOf("${", previousIndex);
		while (nextIndex >= 0) {
			securityPatternTokenized.add(securityPattern.substring(previousIndex, nextIndex));
			final int endIndex = securityPattern.indexOf('}', nextIndex + "${".length());
			Assertion.checkState(endIndex >= nextIndex, "accolade fermante non trouvee : {0} a  {1}", securityPattern, nextIndex);
			final String key = securityPattern.substring(nextIndex + "${".length(), endIndex);
			securityPatternTokenized.add("$" + key);
			previousIndex = endIndex + "}".length();
			nextIndex = securityPattern.indexOf("${", previousIndex);
		}
		if (previousIndex < securityPattern.length()) {
			securityPatternTokenized.add(securityPattern.substring(previousIndex, securityPattern.length()));
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toResourceName(final Object value) {
		final StringBuilder sb = new StringBuilder(securityPattern.length());
		for (final String token : securityPatternTokenized) {
			if (token.startsWith("$")) {
				final String key = token.substring("$".length());
				sb.append(String.valueOf(BeanUtil.getValue(value, key)));
			} else {
				sb.append(token);
			}
		}
		return sb.toString();
	}
}
