/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.ui;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.commons.peg.PegRulesHtmlRenderer;
import io.vertigo.core.definition.Definition;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamox.search.dsl.rules.DslSearchExpressionRule;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.mda.MdaResult;
import io.vertigo.studio.tools.SmartAppConfigBuilder;
import io.vertigo.studio.ui.model.DefinitionTypeModel;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.MapBuilder;
import spark.Response;
import spark.Spark;

public final class Studio {
	private static final Logger LOGGER = LoggerFactory.getLogger(Studio.class);
	private final Configuration configuration;

	private final AutoCloseableApp app;

	/**
	 * Creates a new studio for an existing app
	 * @param app the app we are working on
	 * @param port the port to access the studio interface
	 */
	public Studio(final AutoCloseableApp app, final int port) {
		configuration = new Configuration();
		configuration.setTemplateLoader(new ClassTemplateLoader(Studio.class, "/"));
		configuration.setClassForTemplateLoading(Studio.class, "");
		configuration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
		Spark.setPort(port);
		this.app = app;
	}

	/**
	 * Creates a new app with the given arguments and the associated studio
	 * @param args args for starting the app
	 * @param port the port to access the studio interface
	 */
	public Studio(final String[] args, final int port) {
		configuration = new Configuration();
		configuration.setTemplateLoader(new ClassTemplateLoader(Studio.class, "/"));
		configuration.setClassForTemplateLoading(Studio.class, "");
		configuration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
		Spark.setPort(port);
		final AppConfig appConfig = new SmartAppConfigBuilder(args).build();
		app = new AutoCloseableApp(appConfig);

	}

	/**
	 * Launches the clean goal on the mda manager
	 * @return a result
	 */
	public MdaResult clean() {
		return app.getComponentSpace().resolve(MdaManager.class).clean();
	}

	private MdaResult generate() {
		return app.getComponentSpace().resolve(MdaManager.class).generate();
	}

	private List<DefinitionTypeModel> definitionTypes() {
		final ListBuilder<DefinitionTypeModel> listBuilder = new ListBuilder<>();
		for (final Class<? extends Definition> definitionClass : app.getDefinitionSpace().getAllTypes()) {
			final DefinitionTypeModel definitionTypeModel = new DefinitionTypeModel(definitionClass, app.getDefinitionSpace().getAll(definitionClass));
			listBuilder.add(definitionTypeModel);
		}
		return listBuilder.unmodifiable().build();
	}

	private Definition getDefinition(final String name) {
		return app.getDefinitionSpace().resolve(name, Definition.class);
	}

	/**
	 * Start method of the server
	 */
	public void start() {
		Spark.exception(Exception.class, (e, request, response) -> {
			response.status(500);
			LOGGER.error("studio : error on render ", e);
			response.body(e.getMessage());
		});

		Spark.get("/studio", (request, response) -> {
			final MdaResult mdaResult = null;
			return display(response, mdaResult);
		});

		Spark.get("/grammar", (request, response) -> {
			final MdaResult mdaResult = null;
			return grammar(response, mdaResult);
		});

		Spark.get("/studio/definitions/:definitionName", (request, response) -> {
			final String defintionName = request.params(":definitionName");
			final Map<String, Object> model = new MapBuilder<String, Object>()
					.put("definition", getDefinition(defintionName))
					.build();
			return render(response, "template/definition.ftl", model);
		});

		Spark.get("/generate", (request, response) -> {
			final MdaResult mdaResult = generate();
			return display(response, mdaResult);
		});

		Spark.get("/clean", (request, response) -> {
			final MdaResult mdaResult = clean();
			return display(response, mdaResult);
		});

		Spark.get("/graph", (request, response) -> {
			final ListBuilder<Vertex> verticlesBuilder = new ListBuilder<>();
			final ListBuilder<Definition> edgesBuilders = new ListBuilder<>();
			edgesBuilders.addAll(app.getDefinitionSpace().getAll(DtDefinition.class));
			for (final AssociationNNDefinition associationDefinition1 : app.getDefinitionSpace().getAll(AssociationNNDefinition.class)) {
				verticlesBuilder.add(new Vertex(associationDefinition1.getAssociationNodeA().getDtDefinition(),
						associationDefinition1.getAssociationNodeB().getDtDefinition()));
			}
			for (final AssociationSimpleDefinition associationDefinition2 : app.getDefinitionSpace().getAll(AssociationSimpleDefinition.class)) {
				verticlesBuilder.add(new Vertex(associationDefinition2.getAssociationNodeA().getDtDefinition(),
						associationDefinition2.getAssociationNodeB().getDtDefinition()));
			}

			//					for (final Domain domain : app.getDefinitionSpace().getAll(Domain.class)) {
			//						//						verticlesBuilder.add(new Vertex(domain.getFormatter(), domain));
			//						for (final DefinitionReference<ConstraintDefinition> constraintDefinitionRef : domain.getConstraintDefinitionRefs()) {
			//							verticlesBuilder.add(new Vertex(constraintDefinitionRef.get(), domain));
			//						}
			//					}
			//					edgesBuilders.addAll(app.getDefinitionSpace().getAll(ConstraintDefinition.class));
			//					edgesBuilders.addAll(app.getDefinitionSpace().getAll(Domain.class));

			final Map<String, Object> model = new MapBuilder<String, Object>()
					.put("edges", edgesBuilders.build())
					.put("verticles", verticlesBuilder.build())
					.build();
			return render(response, "template/graph.ftl", model);
		});
	}

	private Object display(final Response response, final MdaResult mdaResult) throws Exception {
		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("definitionTypes", definitionTypes())
				.putNullable("result", mdaResult)
				.build();

		return render(response, "template/studio.ftl", model);
	}

	private Object grammar(final Response response, final MdaResult mdaResult) throws Exception {
		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("searchGrammar", new PegRulesHtmlRenderer().obtainGrammar(new DslSearchExpressionRule()).entrySet())
				.build();

		return render(response, "template/grammar.ftl", model);
	}

	private String render(final Response response, final String templateName, final Map<String, Object> model) throws Exception {
		response.status(200);
		response.type("text/html");

		final StringWriter stringWriter = new StringWriter();

		final Template template = configuration.getTemplate(templateName);
		template.process(model, stringWriter);
		return stringWriter.toString();
	}
}
