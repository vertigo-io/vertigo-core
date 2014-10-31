package io.vertigo.tempo.job;

public final class JobConfig {
	/**
	 * Programme un job pour exécution à une fréquence donnée en secondes.
	 * @param periodInSecond Fréquence d'exécution en secondes
	 */
	public static JobConfig everySecondInterval(final int periodInSecond) {
		return new JobConfig(periodInSecond, -1);
	}

	/**
	 * Programme un job pour exécution chaque jour à heure fixe.
	 * <br/>Si il y a besoin de programmer un job pour exécution à jour fixe dans la semaine
	 * ou dans le mois, il peut être programmé un job chaque puis conditioner l'exécution selon la
	 * date courante en utilisant la classe Calendar.
	 * @param hour Heure fixe d'exécution
	 */
	public static JobConfig everyDayAtHour(final int hour) {
		return new JobConfig(-1, hour);
	}

	//	/**
	//	 * Programme un job pour une seul exécution à une date donnée.
	//	 * @param date Date d'exécution
	//	 */
	//	public static JobConfig atDate(final Date date) {
	//
	//	}

	private final int periodInSecond;
	private final int hour;

	JobConfig(final int periodInSecond, final int hour) {
		this.periodInSecond = periodInSecond;
		this.hour = hour;
	}
}
