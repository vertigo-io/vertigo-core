package io.vertigo.commons.impl.analytics;

import java.net.UnknownHostException;
import java.util.Deque;
import java.util.LinkedList;

import io.vertigo.commons.analytics.AnalyticsAgent;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Agent de collecte.
 * @author pchretien
 */
public final class AnalyticsAgentImpl implements AnalyticsAgent {
	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<Deque<AProcessBuilder>> THREAD_LOCAL_PROCESS = new ThreadLocal<>();

	private static final String KEY_HOST_NAME = "\\{java.io.hostName\\}";
	private final String appName;
	private final String appLocation;
	private final AProcessConnectorPlugin processConnectorPlugin;

	/**
	 * Constructeur.
	 * @param appName app name
	 * @param appLocation location (Environment, Server, Jvm, ..)
	 */
	AnalyticsAgentImpl(
			final String appName,
			final String appLocation,
			final AProcessConnectorPlugin processConnectorPlugin) {
		Assertion.checkArgNotEmpty(appName);
		Assertion.checkArgNotEmpty(appLocation);
		Assertion.checkNotNull(processConnectorPlugin);
		this.appName = appName;
		this.appLocation = translateSystemLocation(appLocation);
		this.processConnectorPlugin = processConnectorPlugin;
	}

	private static String translateSystemLocation(final String systemLocation) {
		try {
			return systemLocation.replaceAll(KEY_HOST_NAME, java.net.InetAddress.getLocalHost().getHostName());
		} catch (final UnknownHostException e) {
			throw new WrappedException(e);
		}
	}

	private static Deque<AProcessBuilder> getStack() {
		final Deque<AProcessBuilder> stack = THREAD_LOCAL_PROCESS.get();
		Assertion.checkNotNull(stack, "Pile non initialisée : startProcess()");
		return stack;
	}

	private static void push(final AProcessBuilder processBuilder) {
		Deque<AProcessBuilder> stack = THREAD_LOCAL_PROCESS.get();
		if (stack == null) {
			stack = new LinkedList<>();
			THREAD_LOCAL_PROCESS.set(stack);
		} else {
			Assertion.checkState(stack.size() < 100, "the stack contains more than 100 process. All processes must be closed.\nStack:" + stack);
		}
		stack.push(processBuilder);
	}

	/**
	 * Démarrage d'un processus.
	 * @param processType Type du processus
	 * @param category Categorie du processus
	 */
	@Override
	public void startProcess(final String type, final String category) {
		final AProcessBuilder processBuilder = new AProcessBuilder(appName, type)
				.withLocation(appLocation)
				.withCategory(category);
		push(processBuilder);
	}

	/**
	 * Incrémente une mesure (set si pas présente).
	 * @param measureType Type de mesure
	 * @param value Incrément de la mesure
	 */
	@Override
	public void incMeasure(final String measureType, final double value) {
		getStack().peek().incMeasure(measureType, value);
	}

	/**
	* Affecte une valeur fixe à la mesure.
	* A utiliser pour les exceptions par exemple (et toute donnée ne s'ajoutant pas).
	* @param measureType Type de mesure
	* @param value valeur de la mesure
	*/
	@Override
	public void setMeasure(final String measureType, final double value) {
		getStack().peek().setMeasure(measureType, value);
	}

	/**
	 * Affecte une valeur fixe à une meta-donnée.
	 *
	 * @param metaDataName Nom de la meta-donnée
	 * @param value Valeur de la meta-donnée
	 */
	@Override
	public void addMetaData(final String metaDataName, final String value) {
		getStack().peek().addMetaData(metaDataName, value);
	}

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas échéant.
	 * @return Process uniquement dans le cas ou c'est le processus parent.
	 */
	/** {@inheritDoc} */
	@Override
	public void stopProcess() {
		final AProcess process = getStack().pop().build();
		if (getStack().isEmpty()) {
			//case of the root process, it's finished and must be sent to the connector
			THREAD_LOCAL_PROCESS.remove(); //Et on le retire du ThreadLocal
			processConnectorPlugin.add(process);
		} else {
			//case of a subProcess, it's finished and must be added to the stack
			getStack().peek().addSubProcess(process);
		}
	}
}
