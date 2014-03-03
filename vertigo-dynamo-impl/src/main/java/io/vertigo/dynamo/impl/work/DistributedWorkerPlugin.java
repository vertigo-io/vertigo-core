package io.vertigo.dynamo.impl.work;

import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.kernel.component.Plugin;

/**
 * Gestion des Workers distribués.
 * 
 * @author npiedeloup, pchretien
 * @version $Id: DistributedWorkerPlugin.java,v 1.5 2013/11/15 15:31:59 pchretien Exp $
 */
public interface DistributedWorkerPlugin extends Worker, Plugin {

	/**
	 * Indique si ce type de work peut-être distribué.
	 * @param work Travail à effectuer
	 * @return si ce type de work peut-être distribué.
	 */
	<WR, W> boolean canProcess(WorkEngineProvider<WR, W> workEngineProvider);

}
