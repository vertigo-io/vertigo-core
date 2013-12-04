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
package io.vertigo.kernel.lang;

import java.util.NoSuchElementException;

/**
 * Classe portant les types optionnels.
 * Inspir�e de la classe Scala de m�me nom.
 *  - option : null ou renseign�e
 *  - none : null
 *  - some : renseign�e 
 *
 * @author jmainaud
 * @param <T> Type de l'objet optionnel.
 */
public final class Option<T> {
	/** Constante d�clarant l'option None. */
	private static final Option<Object> NONE = new Option<>(null);

	/** Valeur. */
	private final T value;

	/**
	 * Cr�e une nouvelle instance de <code>Option</code>.
	 * @param value Valeur null / non null
	 */
	private Option(final T value) {
		this.value = value;
	}

	/**
	 * Donne l'instance de None. 
	 *
	 * @param <T> Type de l'option demand�.
	 * @return None.
	 */
	public static <T> Option<T> none() {
		return (Option<T>) NONE;
	}

	/**
	 * Instance de Some.
	 *
	 * @param <R> Type de l'option.
	 * @param value Valeur de l'option.
	 * @return Option.
	 */
	public static <R> Option<R> some(final R value) {
		Assertion.checkNotNull(value, "Option.some requires a non null value.");
		//---------------------------------------------------------------------
		return new Option<>(value);
	}

	/**
	 * Instance de option.
	 *
	 * @param <T>  Type de l'option.
	 * @param value  Valeur de l'option.
	 * @return L'option.
	 */
	public static <T> Option<T> option(final T value) {
		if (value == null) {
			return none();
		}
		return some(value);
	}

	/**
	 * Indique si l'option est d�finie.
	 *
	 * @return <code>true</code> si d�finie, sinon <code>false</code>.
	 */
	public boolean isDefined() {
		return !isEmpty();
	}

	/**
	 * Indique si l'option n'est pas d�finie.
	 *
	 * @return <code>false</code> si d�finie, sinon <code>true</code>.
	 */
	public boolean isEmpty() {
		return value == null;
	}

	/**
	 * Permet de r�cup�rer le contenu.
	 *
	 * @return Contenu si la valeur est d�finie.
	 * @throws NoSuchElementException Si la valeur n'est pas d�finie.
	 */
	public T get() {
		if (value == null) {
			throw new NoSuchElementException();
		}
		return value;
	}

	/**
	 * Si l'option est d�finie donne la valeur, sinon donne la valeur par d�faut.
	 *
	 * @param defaut Valeur par d�faut.
	 * @return Contenu si la valeur est d�finie.
	 */
	public T getOrElse(final T defaut) {
		return value == null ? defaut : value;
	}

	/**
	 * Permet de r�cup�rer le contenu.
	 *
	 * @param defaut Valeur par d�faut.
	 * @return Contenu si la valeur est d�finie.
	 */
	public Option<T> orElse(final Option<T> defaut) {
		return value == null ? defaut : this;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return value == null ? "" : value.toString();

	}
}
