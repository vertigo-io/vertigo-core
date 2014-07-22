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
package io.vertigo.labs.plugins.job.basic;

import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.job.JobManager;
import io.vertigo.labs.job.SchedulerPlugin;
import io.vertigo.labs.job.metamodel.JobDefinition;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Impl�mentation de JobManager pr�vue pour un serveur sans cluster (une seule JVM).
 *
 * Cette impl�mentation est transactionnelle : une transaction est d�marr�e avant
 * l'appelle de Job.execute() et elle est commit�e s'il n'y a pas d'erreur ou rollback�e sinon.
 *
 * Si ce JobManager est utilis� sur plusieurs JVM en parall�le (ferme ou cluster de serveur
 * d'application), les jobs seront ex�cut�s en parall�le sur chaque instance de serveur.
 *
 * Pour �viter d'ex�cuter les jobs en parall�le, il vous est possible d'initialiser certains
 * jobs sur une seule jvm avec un param�tre d�pendant de l'instance du serveur d'application
 * (un param�tre dans un des contextes tomcat ou une propri�t� syst�me par exemple).
 *
 * Strat�gie impl�ment�e bloque l'ex�cution en parall�le de deux jobs de m�me nom
 * (attention de ne pas multiplier � l'infini les noms de job et donc les threads).
 *
 * Sinon pour avoir des jobs synchronis�s dans une ferme ou un cluster de serveurs, il vous est
 * possible d'utiliser MultiJVMJobManager qui utiliser Quartz d'Open Symphony
 * ou d'impl�menter un JobManager h�ritant de ce JobManager et conditionnant l'ex�cution d'un job
 * dans doExecute � partir d'une synchronisation en base de donn�es.
 *
 * Lorsque close() est appel� sur ce jobManager (undeploy de la webapp), les jobs
 * �ventuellement en cours peuvent tester r�guli�rement Thread.currentThread().isInterrupted() pour
 * savoir s'il est pr�f�rrable de s'arr�ter promptement sans attendre la fin du job.
 *
 * @author evernat
 * @version $Id: BasicSchedulerPlugin.java,v 1.5 2014/02/27 10:27:31 pchretien Exp $
 */
public final class BasicSchedulerPlugin implements SchedulerPlugin, Activeable {
	/**
	 * Pool de timers permettant l'ex�cution des Jobs. 
	 */
	private final TimerPool timerPool = new TimerPool();
	private boolean active;

	/** {@inheritDoc} */
	public void start() {
		active = true;
	}

	/** {@inheritDoc} */
	public void stop() {
		active = false;
		timerPool.close();
	}

	private void checkActive() {
		Assertion.checkArgument(active, "le manager n'est pas dans un �tat actif");
	}

	private Logger getLogger(final String jobName) {
		return Logger.getLogger(jobName);
	}

	/** {@inheritDoc} */
	public void scheduleEverySecondInterval(final JobManager jobManager, final JobDefinition jobDefinition, final int periodInSecond) {
		checkActive();
		Assertion.checkArgument(periodInSecond <= 7 * 24 * 60 * 60, "La p�riode doit �tre inf�rieure � une semaine");
		//---------------------------------------------------------------------
		final TimerTask task = createTimerTask(jobManager, jobDefinition);
		final int startDelay = periodInSecond;
		// on utilise schedule et non scheduleAtFixedRate car c'est la p�riode inter-ex�cution
		// qui importe et non l'intervalle avec la r�f�rence de d�marrage
		// (ainsi pas de rafales si une lenteur momentan�e survient)
		timerPool.getTimer(jobDefinition.getName()).schedule(task, startDelay * 1000L, periodInSecond * 1000L);
		getLogger(jobDefinition.getName()).info("Job " + jobDefinition.getName() + " programm� toutes les " + periodInSecond + " s");
	}

	/** {@inheritDoc} */
	public void scheduleEveryDayAtHour(final JobManager jobManager, final JobDefinition jobDefinition, final int hour) {
		checkActive();
		//a chaque ex�cution il est n�cessaire de reprogrammer l'execution.
		final Date nextExecutionDate = getNextExecutionDate(hour);
		scheduleAtDate(jobManager, jobDefinition, nextExecutionDate);

		//a chaque ex�cution il est n�cessaire de reprogrammer l'execution.
		final Date nextReschedulerDate = new Date(nextExecutionDate.getTime() + 1 * 60 * 1000); //on reprogramme � l'heure dite + 1min (comme on est sur le m^me timer elle passera apr�s
		final TimerTask task = createRescheduledTimerTask(jobManager, jobDefinition, hour);
		timerPool.getTimer(jobDefinition.getName()).schedule(task, nextReschedulerDate);
		log("Tache de reprogrammation du Job ", jobDefinition, nextReschedulerDate);
	}

	private void log(final String info, final JobDefinition jobDefinition, final Date date) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
		getLogger(jobDefinition.getName()).info(info + jobDefinition.getName() + " programm� pour " + dateFormat.format(date));
	}

	/** {@inheritDoc} */
	public void scheduleNow(final JobManager jobManager, final JobDefinition jobDefinition) {
		checkActive();
		final TimerTask task = createTimerTask(jobManager, jobDefinition);
		timerPool.getTimer(jobDefinition.getName()).schedule(task, new Date());
	}

	/** {@inheritDoc} */
	public void scheduleAtDate(final JobManager jobManager, final JobDefinition jobDefinition, final Date date) {
		checkActive();
		final TimerTask task = createTimerTask(jobManager, jobDefinition);
		timerPool.getTimer(jobDefinition.getName()).schedule(task, date);
		log("Job ", jobDefinition, date);
	}

	private static TimerTask createTimerTask(final JobManager jobManager, final JobDefinition jobDefinition) {
		return new BasicTimerTask(jobDefinition, jobManager);
	}

	private static TimerTask createRescheduledTimerTask(final JobManager jobManager, final JobDefinition jobDefinition, final int hour) {
		return new ReschedulerTimerTask(jobManager, jobDefinition, hour);
	}

	private static Date getNextExecutionDate(final int hour) {
		Assertion.checkArgument(hour >= 0 && hour <= 23, "hour doit �tre compris entre 0 et 23");
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
			// on utilise add et non roll pour ne pas tourner en boucle le 31/12
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		return calendar.getTime();
	}

	//---------------------------------------------------------------------------
	//------------------Gestion du rendu et des interactions---------------------
	//---------------------------------------------------------------------------
	//
	//	/** {@inheritDoc} */
	//	@Override
	//	public void toHtml(final PrintStream out) throws Exception {
	//		timerPool.toHtml(out);
	//		out.println("<br/>");
	//		super.toHtml(out);
	//	}
	//
	//	/** {@inheritDoc} */
	//	public ManagerSummaryInfo getMainSummaryInfo() {
	//		final ManagerSummaryInfo managerSummaryInfo = new ManagerSummaryInfo();
	//		managerSummaryInfo.setValue(timerPool.getTimerCount());
	//		managerSummaryInfo.setValueTitle("jobs");
	//		return managerSummaryInfo;
	//	}
	//
	//	/** {@inheritDoc} */
	//	public List<ManagerSummaryInfo> getSummaryInfos() {
	//		final ManagerSummaryInfo managerSummaryInfo = new ManagerSummaryInfo();
	//		managerSummaryInfo.setInfoTitle("SingleJVMJobManager");
	//		return Collections.<ManagerSummaryInfo> singletonList(managerSummaryInfo);
	//	}

	//-------------------------------------------------------------------------
	//----------------------------------POOL de TIMER--------------------------
	//-------------------------------------------------------------------------
	static class TimerPool {
		// cette impl�mentation est simplement bas�e sur la classe java.util.Timer du JDK
		private final Map<String, Timer> timerMap = new HashMap<>();

		// pour interrupt
		private final Map<String, Thread> threadMap = new HashMap<>();

		//		private int getTimerCount() {
		//			return timerMap.size();
		//		}

		synchronized Timer getTimer(final String jobName) {
			//Synchronized car appell�e lors de la programation des Timers, 
			//la plupart sont programm�s dans lors de l'initialisation, 
			//mais il est possible de programmer sur des evenements m�tiers.
			//Utilis� QUE lors des programmations, pas � l'exec.
			Timer timer = timerMap.get(jobName);
			if (timer == null) {
				// le timer est d�mon pour ne pas emp�cher l'arr�t de la jvm,
				// timerName est utilis� comme nom du thread java
				timer = new Timer(jobName, true);
				timerMap.put(jobName, timer);
				final TimerTask registrerThreadTimerTask = new TimerTask() {
					/** {@inheritDoc} */
					@Override
					public void run() {
						registrerTimerThread(Thread.currentThread());
					}
				};
				timer.schedule(registrerThreadTimerTask, new Date());
			}
			return timer;
		}

		void registrerTimerThread(final Thread thread) {
			threadMap.put(thread.getName(), thread);
		}

		void close() {
			//La m�thode close est appel�e par le gestionnaire des managers.
			//Elle n'a pas besoin d'�tre synchronis�e.
			// on cancel les timers pour qu'ils n'aient plus de schedule
			for (final Timer timer : timerMap.values()) {
				timer.cancel();
			}
			timerMap.clear();
			// on appelle interrupt() sur les threads pour qu'un job en cours
			// puisse tester Thread.currentThread().isInterrupted() et s'arr�ter promptement
			for (final Thread thread : threadMap.values()) {
				thread.interrupt();
			}
			threadMap.clear();
		}

		//		void toHtml(final PrintStream out) throws Exception {
		//			out.print("\nNoms des jobs: ");
		//			String separator = "";
		//			for (final String jobName : timerMap.keySet()) {
		//				out.print(separator);
		//				out.print(jobName);
		//				separator = ", ";
		//			}
		//		}
	}

}
