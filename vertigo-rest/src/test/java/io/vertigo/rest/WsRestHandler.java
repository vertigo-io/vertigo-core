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
package io.vertigo.rest;

import io.vertigo.commons.impl.resource.ResourceManagerImpl;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.impl.environment.EnvironmentManagerImpl;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.engines.command.TcpVCommandEngine;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.persona.impl.security.KSecurityManagerImpl;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.rest.filter.CorsAllower;
import io.vertigo.rest.handler.ExceptionHandler;
import io.vertigo.rest.handler.SecurityHandler;
import io.vertigo.rest.handler.SessionHandler;
import io.vertigo.rest.handler.WsRestRoute;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;
import io.vertigoimpl.engines.rest.cmd.ComponentCmdRestServices;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Main WebService Route handler.
 * TODO : make configurable
 * @author npiedeloup 
 */
public final class WsRestHandler {

	public static final class DtDefinitions implements Iterable<Class<?>> {
		public Iterator<Class<?>> iterator() {
			return Arrays.asList(new Class<?>[] { //
					Contact.class, //
					}).iterator();
		}
	}

	public static void main(final String[] args) {
		Spark.setPort(8088);

		//		<final component api="EnvironmentManager" class="io.vertigo.dynamo.impl.environment.EnvironmentManagerImpl">
		//      
		//       <plugin class="io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin" >
		//            <param name ="kpr" value="invs/mdo/execution.kpr"/>
		//        </plugin>  
		//        <plugin class="io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin" />     
		//    </component>

		// @formatter:off 
		// Création de l'état de l'application
		// Initialisation de l'état de l'application
		
		System.out.print(TestUserSession.class);
		final ComponentSpaceConfig config = new ComponentSpaceConfigBuilder()//
				.withSilence(false)//
				//.withRestEngine(new GrizzlyRestEngine(8080))
				.withCommandEngine(new TcpVCommandEngine(4406))//
				.beginModule("commons") //
					.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
						.withParam("locales", "fr")
					.endComponent()
					.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
						.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
					.endComponent()
					.beginComponent(KSecurityManager.class, KSecurityManagerImpl.class)//
						.withParam("userSessionClassName", TestUserSession.class.getName())
					.endComponent() //
				.endModule()
				.beginModule("dynamo").withNoAPI() //
					.beginComponent(EnvironmentManagerImpl.class) //
						.beginPlugin(AnnotationLoaderPlugin.class) //
							.withParam("classes", DtDefinitions.class.getName()).endPlugin()
						.beginPlugin(KprLoaderPlugin.class) //
							.withParam("kpr", "ksp/execution.kpr").endPlugin()
						.beginPlugin(DomainDynamicRegistryPlugin.class).endPlugin()						
					.endComponent()
				.endModule()
				.beginModule("restServices").withNoAPI().withInheritance(RestfulService.class) //
					.beginComponent(ComponentCmdRestServices.class).endComponent() //
					.beginComponent(ContactsRestServices.class).endComponent() //
				.endModule()
				.beginModule("restCore").withNoAPI().withInheritance(Object.class) //
					.beginComponent(RestManager.class).endComponent() //
					.beginComponent(ExceptionHandler.class).endComponent() //
					.beginComponent(SecurityHandler.class).endComponent() //
					.beginComponent(SessionHandler.class).endComponent() //
				.endModule()
				
				.build();
		// @formatter:on
		Home.start(config);

		//Translate EndPoint to route

		final Collection<EndPointDefinition> endPointDefinitions = Home.getDefinitionSpace().getAll(EndPointDefinition.class);

		//test 
		/*Spark.get(new Route("familles") {
			@Override
			public Object handle(final Request request, final Response response) {
				final Object value = famillesRestFulService.readList();
				return new Gson().toJson(value);
			}
		});*/

		// Will serve all static file are under "/public" in classpath if the route isn't consumed by others routes.
		// When using Maven, the "/public" folder is assumed to be in "/main/resources"
		Spark.externalStaticFileLocation("d:/Projets/Projet_Kasper/SPA-Fmk/SPA-skeleton/public/");
		//Spark.before(new IE8CompatibilityFix("8"));
		Spark.before(new CorsAllower());

		for (final EndPointDefinition endPointDefinition : endPointDefinitions) {
			switch (endPointDefinition.getVerb()) {
				case GET:
					Spark.get(new WsRestRoute(endPointDefinition));
					break;
				case POST:
					Spark.post(new WsRestRoute(endPointDefinition));
					break;
				case PUT:
					Spark.put(new WsRestRoute(endPointDefinition));
					break;
				case DELETE:
					Spark.delete(new WsRestRoute(endPointDefinition));
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}

		Spark.get(new Route("/catalog") {
			@Override
			public Object handle(final Request request, final Response response) {
				final Collection<EndPointDefinition> endPointDefCollection = Home.getDefinitionSpace().getAll(EndPointDefinition.class);
				final StringBuilder sb = new StringBuilder();
				for (final EndPointDefinition endPointDefinition : endPointDefCollection) {
					sb.append(endPointDefinition.getVerb().name()).append("://");
					sb.append(endPointDefinition.getPath());
					sb.append("(");
					for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
						sb.append(endPointParam.getType().getSimpleName());
						sb.append(" ");
						sb.append(endPointParam.getName());
					}
					sb.append(")");
					sb.append("\n");
				}
				return sb.toString();
			}
		});
	}

}
