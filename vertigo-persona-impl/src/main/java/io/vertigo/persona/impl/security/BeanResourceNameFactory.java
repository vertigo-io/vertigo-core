package io.vertigo.persona.impl.security;

import io.vertigo.dynamo.domain.util.BeanUtil;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.persona.security.ResourceNameFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ResourceNameFactory standard des beans securisees.
 * @author npiedeloup
 */
public final class BeanResourceNameFactory implements ResourceNameFactory {
	private final String securityPattern;
	private final List<String> securityPatternTokenized = new ArrayList<>();

	/**
	 * Constructeur.
	 * Prend en entr�e le pattern de la chaine de resource � produire. 
	 * Il peut �tre param�tr� avec des propri�t�s de l'objet avec la syntaxe : ${maPropriete}
	 * @param securityPattern Pattern de la resource.
	 */
	public BeanResourceNameFactory(final String securityPattern) {
		io.vertigo.kernel.lang.Assertion.checkNotNull(securityPattern);
		//---------------------------------------------------------------------
		this.securityPattern = securityPattern;
		int previousIndex = 0;
		int nextIndex = securityPattern.indexOf("${", previousIndex);
		while (nextIndex >= 0) {
			securityPatternTokenized.add(securityPattern.substring(previousIndex, nextIndex));
			final int endIndex = securityPattern.indexOf("}", nextIndex + "${".length());
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
