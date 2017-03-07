/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.vertigo.commons.plugins.analytics.log;

import java.util.Collections;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;

/**
 * Processes connector which only use a log4j logger.
 * @author npiedeloup
 */
public final class LoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {
	private static final Gson GSON = new GsonBuilder().create();

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		final Logger logger = Logger.getLogger(process.getType());
		if (logger.isInfoEnabled()) {
			final String json = GSON.toJson(Collections.singletonList(process));
			logger.info(json);
		}
		//		display(0, process);
	}

	//	private void display(final int level, final AProcess process) {
	//		System.out.print("--");
	//		for (int i = 0; i < level; i++) {
	//			System.out.print("--");
	//		}
	//		System.out.println(process.getCategory());
	//		process.getSubProcesses().forEach(subProcess -> display(level + 1, subProcess));
	//	}
	//	private static final char[] PADDING = "        ".toCharArray();
	//	private static final long SEUIL = 20 * 1000L; // 20 secondes
	//	private static final int TIME_SIZE_PADDING = 7;
	//	private static final int TRUNC_SIZE_PARAM = 30;
	//	private static final Logger LOG = Logger.getLogger("component");

	//	if (logger.isInfoEnabled()) {
	//		final StringBuilder sb = new StringBuilder()
	//				.append("Finish ")
	//				.append(category)
	//				.append(success ? " successfully" : " with error").append(" in ( ")
	//				.append(duration)
	//				.append(" ms)");
	//		if (!measures.isEmpty()) {
	//			sb.append(" measures:").append(measures);
	//		}
	//		if (!metaData.isEmpty()) {
	//			sb.append(" metaData:").append(metaData);
	//		}
	//		logger.info(sb.toString());
	//	}
	//
	//	private static long logStart(final AspectMethodInvocation invocation) {
	//		if (LOG.isInfoEnabled()) {
	//			LOG.info("EntrÃ©e " + invocation.getMethod().getDeclaringClass().getSimpleName() + "."
	//					+ invocation.getMethod().getName());
	//		}
	//		return System.currentTimeMillis();
	//	}
	//
	//	private static void log(final Object[] args, final AspectMethodInvocation invocation, final boolean ok,
	//			final long start) {
	//		final long duration = System.currentTimeMillis() - start;
	//		if (LOG.isInfoEnabled() || duration > SEUIL) {
	//			final StringBuilder sb = new StringBuilder();
	//			final String timeLog = String.valueOf(duration);
	//			if (timeLog.length() < TIME_SIZE_PADDING) {
	//				sb.append(PADDING, 0, TIME_SIZE_PADDING - timeLog.length());
	//			}
	//			sb.append(timeLog).append(" ms; ");
	//			if (!ok) {
	//				sb.append("with error; ");
	//			}
	//			sb.append("Facade ").append(invocation.getMethod().getDeclaringClass().getSimpleName())
	//					.append('.')
	//					.append(invocation.getMethod().getName())
	//					.append('(');
	//			appendArgs(sb, checkMaskedParams(args, invocation));
	//			sb.append(')');
	//			if (duration > SEUIL) {
	//				// La façade a dure trop longtemps. On loggue en montant le niveau
	//				LOG.warn(sb.toString());
	//			} else {
	//				LOG.info(sb.toString());
	//			}
	//		}
	//	}
	//
	//	private static Object[] checkMaskedParams(final Object[] args, final AspectMethodInvocation invocation) {
	//		final Object[] ret = new Object[args.length];
	//		final Annotation[][] paramsAnns = invocation.getMethod().getParameterAnnotations();
	//		for (int i = 0; i < args.length; i++) {
	//			final Annotation[] anns = paramsAnns[i];
	//			ret[i] = args[i];
	//			if (mustMaskParams(anns)) {
	//				// On doit masquer
	//				ret[i] = "*****";
	//			}
	//		}
	//		return ret;
	//	}
	//
	//	private static boolean mustMaskParams(final Annotation[] anns) {
	//		boolean mask = false;
	//		if (anns.length != 0) {
	//			// On regarde si on trouve l'annotation de filtrage
	//			for (final Annotation ann : anns) {
	//				if (ann.annotationType().equals(ComponentLoggerParamMasked.class)) {
	//					// On doit masquer
	//					mask = true;
	//				}
	//			}
	//		}
	//		return mask;
	//	}

	//	private static void appendArgs(final StringBuilder sb, final Object[] args) {
	//		if (args == null || args.length == 0) {
	//			return;
	//		}
	//		final int length = args.length;
	//		Object arg;
	//		// si le log est fin ou plus fin on tronque à 256, sinon (info ou warning) on tronque à 40
	//		for (int i = 0; i < length; i++) {
	//			arg = args[i];
	//			if (i != 0) {
	//				sb.append(", ");
	//			}
	//			if (arg == null) {
	//				sb.append("null");
	//			} else {
	//				try {
	//					sb.append(truncString(arg.toString(), TRUNC_SIZE_PARAM).replace('\n', ' '));
	//				} catch (final Exception e) {
	//					// On est en train de génèrer une chaine pour un log
	//					// On s'assure que rien de vient perturber la gé©nération de chaine
	//					LOG.warn("problème lors de l'obtention des paramètres pour le log", e);
	//					sb.append("??");
	//				}
	//			}
	//		}
	//	}
	//
	//	/**
	//	 * Tronque une String à une taille maximum et rajoute ... si tronquée
	//	 *
	//	 * @param s chaine à tronquer
	//	 * @param sizeMax Taille maximum du texte
	//	 * @return Chaine modifiée
	//	 */
	//	private static String truncString(final String s, final int sizeMax) {
	//		if (s == null) {
	//			return null;
	//		}
	//		if (s.length() > sizeMax) {
	//			final String etc = "...";
	//			return s.substring(0, sizeMax - etc.length()) + etc;
	//		}
	//		return s;
	//	}
}
