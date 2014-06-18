package io.vertigo.studio.reporting;

/**
 * Interface représentant un plugin d'analyse.
 * 
 * @author tchassagnette
 * @param <I> Tyep d'objet dont on souhaite obtenir la métrique
 * @param <M> Métrique
 */
public interface MetricEngine<I, M extends Metric> {
	/**
	 * Moteur permettant de calculer une métrique.
	 * @return Métrique obtenue.
	 */
	M execute(final I item);
}
