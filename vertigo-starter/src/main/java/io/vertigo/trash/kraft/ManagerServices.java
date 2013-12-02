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
package vertigoimpl.engines.rest.grizzly.kraft;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import vertigo.kernel.Home;
import vertigo.kernel.lang.JsonExclude;
import vertigo.kernel.lang.Option;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@Path("/components")
public class ManagerServices /*extends AbstractServices*/{
	private final Gson gson = createGson();

	private static Gson createGson() {
		return new GsonBuilder()//
				.setPrettyPrinting()//
				//.serializeNulls()//On veut voir les null
				.registerTypeAdapter(List.class, new JsonSerializer<List>() {

					@Override
					public JsonElement serialize(List src, Type typeOfSrc, JsonSerializationContext context) {
						if (src.isEmpty()) {
							return null;
						}
						return context.serialize(src);
					}
				})//	
				.registerTypeAdapter(Map.class, new JsonSerializer<Map>() {

					@Override
					public JsonElement serialize(Map src, Type typeOfSrc, JsonSerializationContext context) {
						if (src.isEmpty()) {
							return null;
						}
						return context.serialize(src);
					}
				})//
				.registerTypeAdapter(Option.class, new JsonSerializer<Option>() {

					@Override
					public JsonElement serialize(Option src, Type typeOfSrc, JsonSerializationContext context) {
						if (src.isDefined()) {
							return context.serialize(src.get());
						}
						return null; //rien
					}
				})//			
				.registerTypeAdapter(Class.class, new JsonSerializer<Class>() {

					@Override
					public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
						return new JsonPrimitive(src.getName());
					}
				})//
				.addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes arg0) {
						if (arg0.getAnnotation(JsonExclude.class) != null) {
							//						if (arg0.getDeclaredClass().isAssignableFrom(Engine.class)) {
							System.out.println("skipField >" + arg0.getName());
							return true;
						}
						//						System.out.println("not skipField >" + arg0.getName() + ">>" + arg0.getAnnotations());
						return false;
					}

					@Override
					public boolean shouldSkipClass(Class<?> arg0) {
						//						//						if (arg0.isAssignableFrom(List.class)) {
						//						//							return false;
						//						//						}
						//						if (arg0.isInterface()) {
						//							System.out.println("skipClass >" + arg0);
						//							return true;
						//						}
						return false;
					}
				}).create();
	}

	//	@GET()
	//	@Produces(MediaType.TEXT_HTML)
	//	public String getHtmlManagers() {
	//		final Map<String, Object> context = new HashMap<>();
	//		List<Item> items = new ArrayList<>();
	//		for (String id : Home.getComponentSpace().keySet()) {
	//			items.add(new Item(id));
	//		}
	//		context.put("managers", items);
	//		return process("manager", context);
	//	}

	@GET
	@Produces("application/json")
	public String getJSonComponentSpace() {
		return gson.toJson(Home.getComponentSpace().getConfig());
	}
}
