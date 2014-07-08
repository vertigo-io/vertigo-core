package io.vertigo.labs;

import io.vertigo.labs.gedcom.Individual;
import io.vertigo.labs.trait.Commenting;
import io.vertigo.labs.trait.Rating;
import io.vertigo.labs.trait.Tagging;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Attention cette classe est g�n�r�e automatiquement !
 */
public final class DtDefinitions implements Iterable<Class<?>> {
	public Iterator<Class<?>> iterator() {
		return Arrays.asList(new Class<?>[] { //
				Rating.class,//
						Tagging.class,//
						Commenting.class,//
						Individual.class //
				}).iterator();
	}
}
