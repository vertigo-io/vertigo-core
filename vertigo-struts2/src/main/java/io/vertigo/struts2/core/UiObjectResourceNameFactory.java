package io.vertigo.struts2.core;

import io.vertigo.core.lang.Assertion;
import io.vertigo.persona.security.ResourceNameFactory;

/**
 * ResourceNameFactory standard des UiObject s�curis�es.
 * @author npiedeloup
 */
public final class UiObjectResourceNameFactory implements ResourceNameFactory {
	private final ResourceNameFactory beanResourceNameFactory;

	/**
	 * Constructeur.
	 * Prend en entrée le pattern de la chaine de resource à produire. 
	 * Il peut être paramétré avec des propriétés de l'objet avec la syntaxe : ${maPropriete}
	 * @param beanResourceNameFactory BeanResourceNameFactory de la resource.
	 */
	public UiObjectResourceNameFactory(final ResourceNameFactory beanResourceNameFactory) {
		this.beanResourceNameFactory = beanResourceNameFactory;
	}

	/** {@inheritDoc} */
	@Override
	public String toResourceName(final Object value) {
		Assertion.checkArgument(value instanceof UiObject, "La resource est un {0}, elle doit être un UiObject", value.getClass().getSimpleName());
		//---------------------------------------------------------------------
		return beanResourceNameFactory.toResourceName(((UiObject) value).getInnerObject());
	}
}
