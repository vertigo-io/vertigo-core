package io.vertigo.dynamo.work;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

/**
 * Provider des taches.
 * Ce provider définit le moyen dont la tache doit être exécuter.
 * Dans la plupart des cas le moyen est une classe.
 * Dans certain cs il peut s'agir du nom de la classe. 
 * @author  pchretien
 * @version $Id: WorkEngineProvider.java,v 1.4 2014/01/14 18:03:28 npiedeloup Exp $
 */
public final class WorkEngineProvider<WR, W> {
	private static final Injector INJECTOR = new Injector();
	private final String className;
	private final Class<? extends WorkEngine<WR, W>> clazz;
	private final WorkEngine<WR, W> workEngine;

	public WorkEngineProvider(final Class<? extends WorkEngine<WR, W>> clazz) {
		Assertion.checkNotNull(clazz);
		//-----------------------------------------------------------------
		this.clazz = clazz;
		this.className = clazz.getName();
		this.workEngine = null;
	}

	public WorkEngineProvider(final WorkEngine<WR, W> workEngine) {
		Assertion.checkNotNull(workEngine);
		//-----------------------------------------------------------------
		this.workEngine = workEngine;
		this.clazz = null;
		this.className = workEngine.getClass().getName();
	}

	public WorkEngineProvider(final String className) {
		Assertion.checkArgNotEmpty(className);
		//-----------------------------------------------------------------
		this.className = className;
		this.clazz = null;
		this.workEngine = null;
	}

	public WorkEngine<WR, W> provide() {
		if (workEngine != null) {
			return workEngine;
		}
		final Class<? extends WorkEngine<WR, W>> engineClazz;
		if (clazz != null) {
			engineClazz = clazz;
		} else {
			engineClazz = (Class<? extends WorkEngine<WR, W>>) ClassUtil.classForName(className);
		}
		//récupéartion de l'engine par sa classe.
		return INJECTOR.newInstance(engineClazz, Home.getComponentSpace());
	}

	public String getName() {
		return className;
	}

}
