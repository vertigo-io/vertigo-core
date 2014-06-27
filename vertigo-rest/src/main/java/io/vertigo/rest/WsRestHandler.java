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
import io.vertigo.rest.EndPointDefinition.EndPointParam;
import io.vertigo.rest.filter.CorsAllower;
import io.vertigo.rest.handler.ExceptionHandler;
import io.vertigo.rest.handler.SecurityHandler;
import io.vertigo.rest.handler.SessionHandler;
import io.vertigo.rest.handler.WsRestRoute;
import io.vertigo.security.KSecurityManager;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;
import io.vertigoimpl.security.KSecurityManagerImpl;

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
public class WsRestHandler {

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
		final ComponentSpaceConfig config = new ComponentSpaceConfigBuilder()//
				.withSilence(true)//
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
					.beginComponent(FamillesRestfulService.class).endComponent() //
					.beginComponent(ContactsRestfulService.class).endComponent() //
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

		final Collection<EndPointDefinition> endPointDefs = Home.getDefinitionSpace().getAll(EndPointDefinition.class);

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

		for (final EndPointDefinition endPointDefinition : endPointDefs) {
			switch (endPointDefinition.getVerb()) {
				case GET:
					Spark.get(new WsRestRoute(endPointDefinition.getPath(), endPointDefinition));
					break;
				case POST:
					Spark.post(new WsRestRoute(endPointDefinition.getPath(), endPointDefinition));
					break;
				case PUT:
					Spark.put(new WsRestRoute(endPointDefinition.getPath(), endPointDefinition));
					break;
				case DELETE:
					Spark.delete(new WsRestRoute(endPointDefinition.getPath(), endPointDefinition));
					break;
			}
		}

		Spark.get(new Route("/catalogue") {
			@Override
			public Object handle(final Request request, final Response response) {
				final Collection<EndPointDefinition> endPointDefs = Home.getDefinitionSpace().getAll(EndPointDefinition.class);
				final StringBuilder sb = new StringBuilder();
				for (final EndPointDefinition endPointDefinition : endPointDefs) {
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
