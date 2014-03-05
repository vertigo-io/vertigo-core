package io.vertigo.studio.reporting;

/**
 * Interface représentant un plugin d'analyse.
 * 
 * @author tchassagnette
 * @version $Id: MetricEngine.java,v 1.1 2013/07/11 10:04:05 npiedeloup Exp $
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
