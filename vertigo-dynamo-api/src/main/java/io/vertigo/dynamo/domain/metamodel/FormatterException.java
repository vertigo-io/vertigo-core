package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.kernel.exception.VUserException;
import io.vertigo.kernel.lang.MessageKey;
import io.vertigo.kernel.lang.MessageText;

import java.io.Serializable;

/**
 * Exception lancée en cas d'échec de formattage.
 *
 * @author pchretien
 */
public final class FormatterException extends VUserException {
	private static final long serialVersionUID = -7317938262923785123L;

	/**
	 * Constructeur.
	 *
	 * @param key Clé du message externalisé explicitant la raison du non formattage.
	 * @param params Paramètres de la ressource
	 */
	public FormatterException(final MessageKey key, final Serializable... params) {
		super(new MessageText(null, key, params));
	}
}
