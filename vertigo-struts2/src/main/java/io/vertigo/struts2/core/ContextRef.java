package io.vertigo.struts2.core;

import io.vertigo.core.lang.Assertion;

import java.io.File;
import java.io.Serializable;

/**
 * Liste des couples (clé, object) enregistrés.
 * @author npiedeloup
 * @param <O> Type d'objet
 */
public final class ContextRef<O extends Serializable> {
	private final AbstractActionSupport action;
	private final String contextKey;
	private Class<O> valueClass;

	/**
	 * Constructeur.
	 * @param contextKey Clé dans le context
	 * @param action Action struts
	 * @deprecated Utiliser ContextRef(String contextKey, Class<O> valueClass, AbstractActionSupport action)
	 */
	@Deprecated
	public ContextRef(final String contextKey, final AbstractActionSupport action) {
		Assertion.checkArgNotEmpty(contextKey);
		Assertion.checkNotNull(action);
		//---------------------------------------------------------------------
		this.contextKey = contextKey;
		this.action = action;
	}

	/**
	 * Constructeur.
	 * @param contextKey Clé dans le context
	 * @param valueClass Type du paramètre
	 * @param action Action struts
	 */
	public ContextRef(final String contextKey, final Class<O> valueClass, final AbstractActionSupport action) {
		Assertion.checkArgNotEmpty(contextKey);
		Assertion.checkNotNull(action);
		Assertion.checkNotNull(valueClass);
		Assertion.checkArgument(String[].class.equals(valueClass) || String.class.equals(valueClass) || Long.class.equals(valueClass) || Integer.class.equals(valueClass) || Boolean.class.equals(valueClass) || File.class.equals(valueClass), "Le type du paramètre doit être un type primitif (String, Long, Integer, Boolean ou String[]) ou de type File ici {0}.", valueClass.getName());
		//---------------------------------------------------------------------
		this.contextKey = contextKey;
		this.action = action;
		this.valueClass = valueClass;
	}

	/**
	 * @param value Valeur à mettre dans le context
	 */
	public void set(final O value) {
		if (value != null) {
			//TODO valueClass ne doit plus etre Nullable
			if (valueClass == null) {
				valueClass = (Class<O>) value.getClass();
			}
			Assertion.checkArgument(valueClass.isInstance(value), "Cette valeur n'est pas du bon type ({0} au lieu de {1})", value.getClass(), valueClass);
		}
		action.getModel().put(contextKey, value);
	}

	/**
	 * @return Object du context
	 */
	public O get() {
		final Serializable value = action.getModel().get(contextKey);
		//TODO valueClass ne doit plus etre Nullable
		if (valueClass == null && value != null) {
			if (value instanceof String[]) {
				valueClass = (Class<O>) String.class;
			} else {
				valueClass = (Class<O>) value.getClass();
			}
		}
		if (value instanceof String[] && !String[].class.equals(valueClass)) { //cas ou la valeur a été sett�e depuis la request
			final String firstValue = ((String[]) value).length > 0 ? ((String[]) value)[0] : null;
			if (firstValue == null || firstValue.isEmpty()) { //depuis la request : empty == null
				return null;
			} else if (String.class.equals(valueClass)) {
				return valueClass.cast(firstValue);
			} else if (Long.class.equals(valueClass)) {
				return valueClass.cast(Long.valueOf(firstValue));
			} else if (Integer.class.equals(valueClass)) {
				return valueClass.cast(Integer.valueOf(firstValue));
			} else if (Boolean.class.equals(valueClass)) {
				return valueClass.cast(Boolean.valueOf(firstValue));
			}
		}
		if (value instanceof File[]) {
			//TODO revoir la gestion des fichiers
			return valueClass.cast(((File[]) value)[0]);
		}
		return valueClass.cast(value);
	}

	/**
	 * @return Si cet élément est dans le context.
	 */
	public boolean exists() {
		return action.getModel().containsKey(contextKey);
	}
}
