package io.vertigo.app.config.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.BootConfigBuilder;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.json.JsonAppConfig.JsonModuleConfig;
import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.core.component.Plugin;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.Selector;
import io.vertigo.util.Selector.MethodConditions;

public final class JsonAppConfigBuilder implements Builder<AppConfig> {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private final AppConfigBuilder appConfigBuilder = AppConfig.builder();

	private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new MyTypeAdapterFactory()).create();
	private final List<String> activeFlags;

	public JsonAppConfigBuilder(final Properties params) {
		Assertion.checkNotNull(params);
		//---
		activeFlags = Arrays.asList(params.getProperty("boot.activeFlags").split(";"));
		params.remove("boot.activeFlags");
	}

	/**
	 * Begin the boot config of the app.
	 * @return the bootConfig builder
	 */
	public BootConfigBuilder beginBoot() {
		return appConfigBuilder.beginBoot();
	}

	/**
	* Append Config of a set of modules.
	 * @param relativeRootClass Class used to access files in a relative way.
	* @param params properties used to configure the app
	* @param jsonFileNames fileNames of the different json files
	*
	* @return this builder
	*/
	public JsonAppConfigBuilder withFiles(final Class relativeRootClass, final String... jsonFileNames) {
		Assertion.checkNotNull(relativeRootClass);
		Assertion.checkNotNull(jsonFileNames);
		//---
		Stream.of(jsonFileNames)
				.map(xmlModulesFileName -> createURL(xmlModulesFileName, relativeRootClass))
				.forEach(jsonConfigUrl -> handleJsonFileConfig(jsonConfigUrl));
		return this;
	}

	private void handleJsonFileConfig(final URL jsonConfigURL) {
		final JsonAppConfig jsonAppConfig = gson.fromJson(parseFile(jsonConfigURL), JsonAppConfig.class);
		//--- boot
		handleBoot(jsonAppConfig);
		//--- modules
		jsonAppConfig.modules.entrySet().stream()
				.forEach(entry -> handleJsonModuleConfig(entry.getKey(), entry.getValue()));
		//--- initializers
		jsonAppConfig.initializers
				.entrySet()
				.stream()
				.filter(entry -> isEnabledByFlag(getFlagsOfJsonObject(entry.getValue()))) // filter activated initializers only
				.forEach(entry -> appConfigBuilder.addInitializer(ClassUtil.classForName(entry.getKey(), ComponentInitializer.class)));
	}

	private void handleBoot(final JsonAppConfig jsonAppConfig) {
		if (jsonAppConfig.boot != null) {
			final String locales = jsonAppConfig.boot.params.get("locales");
			final String defaultZoneId = jsonAppConfig.boot.params.get("defaultZoneId");
			if (locales != null) {
				if (defaultZoneId == null) {
					appConfigBuilder
							.beginBoot()
							.withLocales(locales);
				} else {
					appConfigBuilder
							.beginBoot()
							.withLocalesAndDefaultZoneId(locales, defaultZoneId);
				}
			}
			jsonAppConfig.boot.plugins.forEach(
					plugin -> {
						Assertion.checkState(plugin.size() == 1, "a plugin is defined by it's class");
						// ---
						final String pluginClassName = plugin.keySet().iterator().next();
						if (isEnabledByFlag(getFlagsOfJsonObject(plugin.get(pluginClassName)))) {
							appConfigBuilder.beginBoot()
									.addPlugin(
											ClassUtil.classForName(pluginClassName, Plugin.class),
											plugin.get(pluginClassName).entrySet().stream()
													.filter(entry -> !"__flags__".equals(entry.getKey()))
													.map(entry -> Param.of(entry.getKey(), entry.getValue().getAsString()))
													.toArray(Param[]::new));
						}
					});
		}
	}

	private void handleJsonModuleConfig(final String featuresClassName, final JsonModuleConfig jsonModuleConfig) {
		if (isEnabledByFlag(jsonModuleConfig.flags)) {
			final Features moduleConfigByFeatures = ClassUtil.newInstance(featuresClassName, Features.class);
			final Map<String, Method> featureMethods = new Selector().from(moduleConfigByFeatures.getClass())
					.filterMethods(MethodConditions.annotatedWith(Feature.class))
					.findMethods()
					.stream()
					.map(classAndMethod -> classAndMethod.getVal2())
					.collect(Collectors.toMap(method -> method.getAnnotation(Feature.class).value(), method -> method));

			if (jsonModuleConfig.features != null) {
				jsonModuleConfig.features.entrySet()
						.stream()
						.forEach(entry -> {
							final Method methodForFeature = featureMethods.get(entry.getKey());
							Assertion.checkNotNull(methodForFeature);
							entry.getValue()
									.stream()
									.filter(jsonObject -> isEnabledByFlag(getFlagsOfJsonObject(jsonObject)))
									.forEach(
											jsonParamsConfig -> ClassUtil.invoke(moduleConfigByFeatures, methodForFeature, findmethodParameters(jsonParamsConfig, methodForFeature, entry.getKey(), featuresClassName)));
						});
			}

			jsonModuleConfig.plugins.forEach(
					plugin -> {
						Assertion.checkState(plugin.size() == 1, "a plugin is defined by it's class");
						// ---
						final String pluginClassName = plugin.keySet().iterator().next();
						if (isEnabledByFlag(getFlagsOfJsonObject(plugin.get(pluginClassName)))) {
							moduleConfigByFeatures
									.addPlugin(
											ClassUtil.classForName(pluginClassName, Plugin.class),
											plugin.get(pluginClassName).entrySet().stream()
													.filter(entry -> !"__flags__".equals(entry.getKey()))
													.map(entry -> Param.of(entry.getKey(), entry.getValue().getAsString()))
													.toArray(Param[]::new));
						}
					});
			appConfigBuilder.addModule(moduleConfigByFeatures.build());
		}
	}

	private static List<String> getFlagsOfJsonObject(final JsonObject jsonObject) {
		if (!jsonObject.has("__flags__")) {
			return Collections.emptyList();
		}
		return StreamSupport.stream(jsonObject.getAsJsonArray("__flags__").spliterator(), false)
				.map(flag -> flag.getAsString())
				.collect(Collectors.toList());
	}

	private boolean isEnabledByFlag(final List<String> flags) {
		Assertion.checkNotNull(flags);
		//---
		if (flags.isEmpty()) {
			return true;// no flags declared means always
		}
		return flags.stream()
				.anyMatch(flag -> activeFlags.contains(flag));
	}

	private static Object[] findmethodParameters(final JsonObject jsonParamsConfig, final Method method, final String featureName, final String featuresClassName) {
		Assertion.checkState(method.getParameterCount() <= 1, "A feature method can have 0 parameter or a single Param... parameter");
		if (method.getParameterCount() == 1) {
			final Param[] params = jsonParamsConfig.entrySet()
					.stream()
					.filter(jsonEntry -> !"__flags__".equals(jsonEntry.getKey()))
					.map(paramEntry -> Param.of(paramEntry.getKey(), paramEntry.getValue().getAsString()))
					.toArray(Param[]::new);
			return new Object[] { params };
		}
		return EMPTY_ARRAY;

		//		return Stream.of(method.getParameters())
		//				.map(parameter -> {
		//					//Assertion.checkState(parameter.isAnnotationPresent(Named.class), "Params of a feature must be annotated with @Named");
		//					// ---
		//					final String paramName = parameter.getAnnotation(Named.class).value();
		//					final Class paramType = parameter.getType();
		//					final Class trueParamType;
		//					final boolean isParamOptional = paramType.isAssignableFrom(Optional.class);
		//					if (isParamOptional) {
		//						trueParamType = ClassUtil.getGeneric(parameter.getParameterizedType(), () -> new UnsupportedOperationException("La détection du générique n'a pas pu être effectuée sur le constructeur "));
		//					} else {
		//						trueParamType = paramType;
		//					}
		//					Assertion.checkState(trueParamType.isAssignableFrom(String.class), "Param '{0}' of a feature '{1}' must be String or Optional<String>", paramName, featureName);
		//					// ---
		//					final String paramValue = jsonParamsConfig.get(paramName);
		//					if (isParamOptional) {
		//						return Optional.ofNullable(paramValue);
		//					}
		//					Assertion.checkNotNull(paramValue, "No value provided for param '{0}' on feature '{1}' in the module '{2}'", paramName, featureName, featuresClassName);
		//					return paramValue;
		//				})
		//				.toArray();
	}

	/**
	 * @param logConfig Config of logs
	 * @return  this builder
	 */
	public JsonAppConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.checkNotNull(logConfig);
		//-----
		appConfigBuilder.beginBoot().withLogConfig(logConfig).endBoot();
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AppConfig build() {
		return appConfigBuilder.build();
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 *
	 * @param fileName Nom du fichier
	 * @return URL non null
	 */
	private static URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//-----
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath
			final URL url = relativeRootClass.getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}

	private static String parseFile(final URL url) {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error reading json file : '{0}'", url);
		}
	}

	private final class MyTypeAdapterFactory<E> implements TypeAdapterFactory {

		@Override
		public <T> TypeAdapter<T> create(final Gson myGson, final TypeToken<T> type) {
			if (List.class.isAssignableFrom(type.getRawType())) {
				final Class elementType = ClassUtil.getGeneric(type.getType(), () -> new VSystemException("toot"));
				final TypeAdapter<E> elementTypeAdapter = gson.getAdapter(elementType);
				return new TypeAdapter<T>() {

					@Override
					public void write(final JsonWriter out, final T value) throws IOException {
						//nothing
					}

					@Override
					public T read(final JsonReader in) throws IOException {
						// This is where we detect the list "type"
						final List<E> list = new ArrayList<>();
						final JsonToken token = in.peek();
						switch (token) {
							case BEGIN_ARRAY:
								// If it's a regular list, just consume [, <all elements>, and ]
								in.beginArray();
								while (in.hasNext()) {
									list.add(elementTypeAdapter.read(in));
								}
								in.endArray();
								break;
							case BEGIN_OBJECT:
							case STRING:
							case NUMBER:
							case BOOLEAN:
								// An object or a primitive? Just add the current value to the result list
								list.add(elementTypeAdapter.read(in));
								break;
							case NULL:
								throw new AssertionError("Must never happen: check if the type adapter configured with .nullSafe()");
							case NAME:
							case END_ARRAY:
							case END_OBJECT:
							case END_DOCUMENT:
								throw new MalformedJsonException("Unexpected token: " + token);
							default:
								throw new AssertionError("Must never happen: " + token);
						}
						return (T) list;

					}

				};

			}
			return null;
		}

	}

}
