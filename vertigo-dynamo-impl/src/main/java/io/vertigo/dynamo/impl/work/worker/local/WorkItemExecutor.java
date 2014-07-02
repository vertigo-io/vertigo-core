package io.vertigo.dynamo.impl.work.worker.local;

import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

/**
 * Exécution des works.
 * 
 * @author pchretien, npiedeloup
 * @param <WR> Type du résultat
 * @param <W> Type du work
 */
public final class WorkItemExecutor<WR, W> implements Runnable {

	/**
	 * Pour vider les threadLocal entre deux utilisations du Thread dans le pool,
	 * on garde un accès débridé au field threadLocals de Thread.
	 * On fait le choix de ne pas vider inheritedThreadLocal qui est moins utilisé.
	 */
	private static final Field threadLocalsField;
	static {
		try {
			threadLocalsField = Thread.class.getDeclaredField("threadLocals");
		} catch (final SecurityException e) {
			throw new VRuntimeException(e);
		} catch (final NoSuchFieldException e) {
			throw new VRuntimeException(e);
		}
		threadLocalsField.setAccessible(true);
	}

	private final Worker worker;
	private final WorkItem<WR, W> workItem;
	private final Logger logger = Logger.getLogger(WorkManager.class); //même logger que le WorkListenerImpl

	/**
	 * Constructeur.
	 * @param workItem WorkItem à traiter
	 */
	public WorkItemExecutor(final Worker worker, final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(worker);
		Assertion.checkNotNull(workItem);
		//-----------------------------------------------------------------
		this.workItem = workItem;
		this.worker = worker;
	}

	/** {@inheritDoc} */
	public void run() {
		doExecute();
	}

	private void doExecute() {
		Assertion.checkNotNull(workItem);
		//---------------------------------------------------------------------
		try {
			workItem.getWorkResultHandler().get().onStart();
			worker.process(workItem);
			workItem.getWorkResultHandler().get().onSuccess(workItem.getResult());
		} catch (final Throwable t) {
			workItem.getWorkResultHandler().get().onFailure(t);
			logError(t);
		} finally {
			try {
				//Vide le threadLocal
				cleanThreadLocals();
			} catch (final VRuntimeException e) {
				//Ce n'est pas une cause de rejet du Work, on ne fait que logger
				logError(e);
			}
		}
	}

	private void logError(final Throwable e) {
		logger.error("Erreur de la tache de type : " + workItem.getWorkEngineProvider().getName(), e);
	}

	/**
	 * Vide le threadLocal du thread avant de le remettre dans le pool.
	 * Ceci protège contre les WorkEngine utilsant un ThreadLocal sans le vider. 
	 * Ces workEngine peuvent poser des problémes de fuites mémoires (Ex: le FastDateParser de Talend)
	 * Voir aussi: http://weblogs.java.net/blog/jjviana/archive/2010/06/10/threadlocal-thread-pool-bad-idea-or-dealing-apparent-glassfish-memor
	 */
	private static void cleanThreadLocals() {
		try {
			threadLocalsField.set(Thread.currentThread(), null);
		} catch (final IllegalArgumentException e) {
			throw new VRuntimeException(e);
		} catch (final IllegalAccessException e) {
			throw new VRuntimeException(e);
		}
	}
}
