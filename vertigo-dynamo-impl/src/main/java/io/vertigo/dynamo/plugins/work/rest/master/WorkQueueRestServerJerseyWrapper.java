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
package io.vertigo.dynamo.plugins.work.rest.master;

import io.vertigo.dynamo.impl.work.MasterPlugin;
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
 * WorkQueue distribuée - partie serveur en REST.
 * @author npiedeloup
 */
@Path("/workQueue")
@Singleton
public final class WorkQueueRestServerJerseyWrapper {

	private WorkQueueRestServer getWorkQueueRestServer() {
		final RestMasterPlugin masterPlugin = (RestMasterPlugin) Home.getComponentSpace().resolve("masterPlugin", MasterPlugin.class);
		return masterPlugin.getWorkQueueRestServer();
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
		getWorkQueueRestServer().onDone(true, uuid, base64Result);
	}

	@POST
	@Path("/event/failure/{uuid}")
	@Consumes(MediaType.TEXT_PLAIN)
	public void onFailure(@PathParam("uuid") final String uuid, final String base64Result) {
		//---------------------------------------------------------------------
		getWorkQueueRestServer().onDone(false, uuid, base64Result);
	}

	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		//---------------------------------------------------------------------
		return getWorkQueueRestServer().getVersion();
	}
}
