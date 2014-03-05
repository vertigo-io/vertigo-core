package io.vertigo.studio.plugins.mda.search;

import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Génération des objets relatifs au module Search. 
 *  
 * @author dchallas
 * @version $Id: SearchGeneratorPlugin.java,v 1.6 2014/02/27 10:30:06 pchretien Exp $
 */
public final class SearchGeneratorPlugin extends AbstractGeneratorPlugin<SearchConfiguration> {
	/** {@inheritDoc}  */
	public SearchConfiguration createConfiguration(final Properties properties) {
		return new SearchConfiguration(properties);
	}

	/** {@inheritDoc}  */
	public void generate(final SearchConfiguration searchConfiguration, final Result result) {
		Assertion.checkNotNull(searchConfiguration);
		Assertion.checkNotNull(result);
		//---------------------------------------------------------------------
		generateDtDefinitions(searchConfiguration, result);
	}

	private void generateDtDefinitions(final SearchConfiguration searchConfiguration, final Result result) {
		for (final IndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(IndexDefinition.class)) {
			generateSchema(searchConfiguration, result, indexDefinition);
		}
	}

	private void generateSchema(final SearchConfiguration searchConfiguration, final Result result, final IndexDefinition indexDefinition) {
		/** Registry */
		final Map<String, Object> mapRoot = new HashMap<>();
		mapRoot.put("indexDefinition", indexDefinition);
		mapRoot.put("indexType", new TemplateMethodIndexType());

		final FileGenerator super2java = getFileGenerator(searchConfiguration, mapRoot, "schema", "solr/" + indexDefinition.getName() + "/conf", ".xml", "search/schema.ftl");
		super2java.generateFile(result, true);
	}
}
