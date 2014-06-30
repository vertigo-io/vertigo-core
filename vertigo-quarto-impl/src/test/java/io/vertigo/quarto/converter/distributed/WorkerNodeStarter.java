package io.vertigo.quarto.converter.distributed;

import io.vertigo.dynamo.work.distributed.rest.Starter;
import io.vertigo.kernel.lang.Option;

import java.util.Properties;

/**
 * @author npiedeloup
 * $Id: WorkerNodeStarter.java,v 1.2 2013/10/22 10:54:08 pchretien Exp $
 */
public final class WorkerNodeStarter {

	/**
	 * Lance l'environnement et attend ind�finiment.
	 * @param args "Usage: java vertigo.kernel.Starter managers.xml <conf.properties>"
	 */
	public static void main(final String[] args) {
		final Starter starter = new Starter("./managers-node-test.xml", Option.<String> none(), WorkerNodeStarter.class, Option.<Properties> none(), args.length == 1 ? Long.parseLong(args[0]) * 1000L : 5 * 60 * 1000L);

		System.out.println("Node starting");
		starter.run();
		System.out.println("Node stop");
	}
}
