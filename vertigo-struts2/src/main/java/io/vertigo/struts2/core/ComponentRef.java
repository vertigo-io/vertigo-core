package io.vertigo.struts2.core;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Assertion;

import java.io.Serializable;

/**
 * Référence vers un composant.
 * Permet d'assurer le référencement du composant hors de l'injecteur.
 * Et eventuellement le référencement reporté au premier appel (lazyLoading). 
 * 
 * @author pchretien, npiedeloup
 * @param <T> Type du composant
 */
public final class ComponentRef<T> implements Serializable {
	private static final long serialVersionUID = -3692640153710870256L;
	private transient T instance;
	private final String componentId;
	private final Class<T> componentClazz;

	/**
	 * @param componentClazz Type du composant
	 * @return Référence vers ce composant
	 */
	public static <T> ComponentRef<T> makeRef(final Class<T> componentClazz) {
		return new ComponentRef<>(componentClazz, false);
	}

	/**
	 * @param componentClazz Type du composant
	 * @return Référence résolue en lazy loading vers ce composant
	 */
	public static <T> ComponentRef<T> makeLazyRef(final Class<T> componentClazz) {
		return new ComponentRef<>(componentClazz, true);
	}

	/**
	 * Constructeur.
	 * @param componentClazz Class du composant
	 * @param lazy Si référencement à la première demande
	 */
	private ComponentRef(final Class<T> componentClazz, final boolean lazy) {
		Assertion.checkNotNull(componentClazz);
		//---------------------------------------------------------------------
		componentId = null;
		this.componentClazz = componentClazz;
		if (!lazy) {
			get();
		}
	}

	/**
	 * Constructeur.
	 * @param componentId Id du composant
	 * @param componentClazz Class du composant
	 * @param lazy Si référencement à la première demande
	 */
	private ComponentRef(final String componentId, final Class<T> componentClazz, final boolean lazy) {
		Assertion.checkArgNotEmpty(componentId);
		Assertion.checkNotNull(componentClazz);
		//---------------------------------------------------------------------
		this.componentId = componentId;
		this.componentClazz = componentClazz;
		if (!lazy) {
			get();
		}
	}

	/**
	 * @return Element pointé par la référence 
	 */
	//le synchronized à peu d'impact sur une référence qui à vocation à être instanciée à chaque usage
	// TODO a voir si on le retire.
	public synchronized T get() {
		if (instance == null) {
			if (componentId != null) {
				instance = Home.getComponentSpace().resolve(componentId, componentClazz);
			} else {
				instance = Home.getComponentSpace().resolve(componentClazz);
			}
		}
		return instance;
	}
}
