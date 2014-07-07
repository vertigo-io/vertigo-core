package io.vertigo.dynamo.plugins.work.rest;

import io.vertigo.dynamo.impl.work.DistributedWorkerPlugin;
import io.vertigo.kernel.Home;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.spi.resource.Singleton;

/**
 * WorkQueue distribu�e - partie serveur en REST.
 * @author npiedeloup
 * @version $Id: WorkQueueRestServerJerseyWrapper.java,v 1.6 2014/02/03 17:28:45 pchretien Exp $
 */
@Path("/workQueue")
@Singleton
public final class WorkQueueRestServerJerseyWrapper {

	private WorkQueueRestServer getWorkQueueRestServer() {
		final RestDistributedWorkerPlugin distributedWorkerPlugin = (RestDistributedWorkerPlugin) Home.getComponentSpace().resolve("distributedWorkerPlugin", DistributedWorkerPlugin.class);
		return distributedWorkerPlugin.getWorkQueueRestServer();
	}

	@GET
	@Path("/pollWork/{workType}")
	public String pollWork(@PathParam("workType") final String workType, @QueryParam("nodeUID") final String nodeUID) {
		//---------------------------------------------------------------------
		return getWorkQueueRestServer().pollWork(workType, nodeUID);
	}

	@POST
	@Path("/event/start/{uuid}")
	public void onStart(@PathParam("uuid") final String uuid) {
		//---------------------------------------------------------------------
		getWorkQueueRestServer().onStart(uuid);
	}

	@POST
	@Path("/event/success/{uuid}")
	@Consumes(MediaType.TEXT_PLAIN)
	public void onSuccess(@PathParam("uuid") final String uuid, final String base64Result) {
		//---------------------------------------------------------------------
		getWorkQueueRestServer().onSuccess(uuid, base64Result);
	}

	@POST
	@Path("/event/failure/{uuid}")
	@Consumes(MediaType.TEXT_PLAIN)
	public void onFailure(@PathParam("uuid") final String uuid, final String base64Result) {
		//---------------------------------------------------------------------
		getWorkQueueRestServer().onFailure(uuid, base64Result);
	}

	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		//---------------------------------------------------------------------
		return getWorkQueueRestServer().getVersion();
	}
}
