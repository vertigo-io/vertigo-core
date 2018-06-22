package io.vertigo.studio.plugins.mda.task.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorBuilder;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.task.model.TaskDefinitionModel;
import io.vertigo.util.MapBuilder;
import io.vertigo.util.StringUtil;

/**
 * Génération des objets relatifs au module Task.
 *
 * @author sezratty
 */
public final class TaskTestGeneratorPlugin implements GeneratorPlugin {

	private final String targetSubDir;
	private final String baseTestClass;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 * @param baseTestClass Nom canonique de la classe de base pour les tests.
	 */
	@Inject
	public TaskTestGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir,
			@Named("baseTestClass") final String baseTestClass) {
		//-----
		this.targetSubDir = targetSubDir;
		this.baseTestClass = baseTestClass;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		
		List<TemplateTestSuite> aoSuites = new ArrayList<>();
		
		generatePaos(targetSubDir, fileGeneratorConfig, mdaResultBuilder, aoSuites);
		generateDaos(targetSubDir, fileGeneratorConfig, mdaResultBuilder, aoSuites);
		
		generateAllAoSuite(targetSubDir, fileGeneratorConfig, mdaResultBuilder, aoSuites);
	}

	/**
	 * Génération de tous les PAOs.
	 */
	private void generatePaos(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final List<TemplateTestSuite> aoSuites) {
		
		
		//On liste des taches regroupées par Package.
		for (final Entry<String, List<TaskDefinition>> entry : buildPackageMap().entrySet()) {
			final Collection<TaskDefinition> taskDefinitionCollection = entry.getValue();
			if (!taskDefinitionCollection.isEmpty()) {
				
				final String packageName = entry.getKey();
				final String className = getLastPackagename(packageName) + "PAO";
				
				generateAo(targetSubDir, fileGeneratorConfig, mdaResultBuilder, taskDefinitionCollection, packageName,
						className, aoSuites);
			}
		}
		
	}

	private static void generateAllAoSuite(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder, List<TemplateTestSuite> aoSuites) {

		if(aoSuites.isEmpty()){
			return;
		}
		
		List<TemplateTestClass> testClasses = new ArrayList<>();
		for(TemplateTestSuite taskSuite : aoSuites){
			TemplateTestClass testClass = new TemplateTestClass(taskSuite.getPackageName(), "AllTests");
			testClasses.add(testClass);
		}
		
		/* Calcule le nom du package DAO à partir de la première suite. */
		final TemplateTestClass templateTestClass = testClasses.get(0);
		final String allPaoPackageName = getDaoPackageName(templateTestClass.getPackageName());
		
		TemplateTestSuite suiteModel = new TemplateTestSuite(testClasses, allPaoPackageName);		
		generateAllPaoSuite(targetSubDir, fileGeneratorConfig, mdaResultBuilder, allPaoPackageName, suiteModel);
	}

	private static String getDaoPackageName(final String packageName) {
		List<String> parts = new ArrayList<>();
		for(String part : packageName.split("\\.")){
			parts.add(part);
			if("dao".equals(part)) {
				break;
			}
		}
		return String.join(".", parts);
	}

	/**
	 * Génération de tous les DAOs.
	 */
	private void generateDaos(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final List<TemplateTestSuite> aoSuites) {		
		
		for (final Entry<DtDefinition, List<TaskDefinition>> entry : builDtDefinitiondMap().entrySet()) {
			final DtDefinition dtDefinition = entry.getKey();
			if (dtDefinition.isPersistent()) {
				final String definitionPackageName = dtDefinition.getPackageName();
				final String packageNamePrefix = fileGeneratorConfig.getProjectPackageName() + ".domain";
				final String packageName = fileGeneratorConfig.getProjectPackageName() + ".dao" + definitionPackageName.substring(packageNamePrefix.length());
				
				final String className = dtDefinition.getClassSimpleName() + "DAO";
				
				generateAo(targetSubDir, fileGeneratorConfig, mdaResultBuilder, entry.getValue(), packageName,
						className, aoSuites);
			}
		}
	}

	private void generateAo(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder, final Collection<TaskDefinition> taskDefinitionCollection,
			final String packageName, final String className, final List<TemplateTestSuite> paoSuites) {
		List<TemplateTestClass> testClasses = new ArrayList<>();
		for (final TaskDefinition taskDefinition : taskDefinitionCollection) {
			final TemplateAoTaskTest paoModel = new TemplateAoTaskTest(fileGeneratorConfig, taskDefinition, packageName, className, baseTestClass);
			TemplateTestClass testClass = new TemplateTestClass(paoModel.getTaskDefinition().getTestPackageName(), paoModel.getTaskDefinition().getTestClassName());
			testClasses.add(testClass);
			
			generatePaoTaskTest(targetSubDir, fileGeneratorConfig, mdaResultBuilder, paoModel);
		}

		/* Génération de la suite des tests des tasks de tout le PAO. */
		if(!testClasses.isEmpty()){			
			String suitePackageName = testClasses.get(0).getPackageName();
			final TemplateTestSuite suiteModel = new TemplateTestSuite(testClasses, suitePackageName);
			paoSuites.add(suiteModel);
			
			generatePaoSuite(targetSubDir, fileGeneratorConfig, mdaResultBuilder, suitePackageName, suiteModel);
		}
	}
	
	/**
	 * Retourne le nom du package feuille à partir d'un nom complet de package.
	 * exemple : org.company.sugar > sugar
	 * @param packageName Nom de package
	 * @return Nom du package feuille à partir d'un nom complet de package
	 */
	private static String getLastPackagename(final String packageName) {
		String lastPackageName = packageName;
		if (lastPackageName.indexOf('.') != -1) {
			lastPackageName = lastPackageName.substring(lastPackageName.lastIndexOf('.') + 1);
		}
		return StringUtil.first2UpperCase(lastPackageName);
	}

	private static void generatePaoSuite(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder, String suitePackageName, final TemplateTestSuite suiteModel) {
		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("suite", suiteModel)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName("AllTests.java")
				.withGenSubDir(targetSubDir)
				.withPackageName(suitePackageName)
				.withTemplateName("task/test/test_suite.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	private static void generateAllPaoSuite(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder, String suitePackageName, final TemplateTestSuite suiteModel) {
		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("suite", suiteModel)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName("AllTestsAo.java")
				.withGenSubDir(targetSubDir)
				.withPackageName(suitePackageName)
				.withTemplateName("task/test/test_suite_inline_class_name.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	private static void generatePaoTaskTest(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder, final TemplateAoTaskTest paoModel) {
		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("pao", paoModel)
				.build();

		final String testPackageName = paoModel.getTaskDefinition().getTestPackageName();
		final String fileName = paoModel.getTaskDefinition().getTestClassName() + ".java";
		
		/* Calcule le chemin du fichier à générer. */
		final String directoryPath = fileGeneratorConfig.getTargetGenDir() + targetSubDir + File.separatorChar + package2directory(testPackageName) + File.separatorChar;
		final String fullFilePath = directoryPath + fileName;
		
		/* Vérifie que le fichier n'existe pas déjà. */
		if(new File(fullFilePath).exists()){
			/* Le fichier existe : on ne l'écrase pas. */
			return;
		}
		
		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(fileName)
				.withGenSubDir(targetSubDir)
				.withPackageName(testPackageName)
				.withTemplateName("task/test/ao_task_test.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}
	
	private static String package2directory(final String packageName) {
		return packageName.replace('.', File.separatorChar).replace('\\', File.separatorChar);
	}

	/**
	 * Stratégie pour savoir si une tache est PAO ou DAO.
	 * Si la DT est non null DAO sinon PAO.
	 */
	private static DtDefinition getDtDefinition(final TaskDefinitionModel templateTaskDefinition) {
		if (templateTaskDefinition.isOut()) {
			//si out on regarde si en sortie on a un DTO ou une DTC typé.
			final Domain outDomain = templateTaskDefinition.getOutAttribute().getDomain();
			if (outDomain.getScope().isDataObject()) {
				return outDomain.getDtDefinition();
			}
			return null;
		}
		//there is no OUT param
		//We are searching igf there is an no-ambiguous IN param defined as a DataObject(DTO or DTC)
		final List<Domain> candidates = templateTaskDefinition.getInAttributes()
				.stream()
				.map(taskAtributeModel -> taskAtributeModel.getDomain())
				.filter(domain -> domain.getScope().isDataObject())
				.collect(Collectors.toList());
		//There MUST be only ONE candidate
		if (candidates.size() == 1) {
			return candidates.get(0).getDtDefinition();
		}
		//Ambiguosity => PAO
		return null;
	}

	private static Map<String, List<TaskDefinition>> buildPackageMap() {
		final Collection<TaskDefinition> taskDefinitions = Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class);
		final Map<String, List<TaskDefinition>> taskDefinitionsMap = new LinkedHashMap<>();
		//---
		for (final TaskDefinition taskDefinition : taskDefinitions) {
			final TaskDefinitionModel templateTaskDefinition = new TaskDefinitionModel(taskDefinition);
			final DtDefinition dtDefinition = getDtDefinition(templateTaskDefinition);
			// Correction bug : task avec retour DtObject (non persistant) non générée
			//Les taches sont générées dans les pao
			// - si il n'esxiste pas de définition associées à la tache
			// - ou si la définition est considérée comme non persistante.
			final boolean pao = dtDefinition == null || !dtDefinition.isPersistent();
			if (pao) {
				//La tache est liée au package. (PAO)
				final List<TaskDefinition> list = taskDefinitionsMap
						.computeIfAbsent(taskDefinition.getPackageName(), k -> new ArrayList<>());
				//on ajoute la tache aux taches du package.
				list.add(taskDefinition);
			}
		}
		return taskDefinitionsMap;

	}

	private static Map<DtDefinition, List<TaskDefinition>> builDtDefinitiondMap() {
		final Collection<TaskDefinition> taskDefinitions = Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class);
		final Map<DtDefinition, List<TaskDefinition>> taskDefinitionsMap = new LinkedHashMap<>();

		//---
		//Par défaut, On crée pour chaque DT une liste vide des taches lui étant associées.
		final Collection<DtDefinition> dtDefinitions = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class);
		for (final DtDefinition dtDefinition : dtDefinitions) {
			taskDefinitionsMap.put(dtDefinition, new ArrayList<TaskDefinition>());
		}
		//---
		for (final TaskDefinition taskDefinition : taskDefinitions) {
			final TaskDefinitionModel templateTaskDefinition = new TaskDefinitionModel(taskDefinition);

			final DtDefinition dtDefinition = getDtDefinition(templateTaskDefinition);
			final boolean dao = dtDefinition != null;
			if (dao) {
				//Dans le cas d'un DTO ou DTC en sortie on considère que la tache est liée au DAO.
				taskDefinitionsMap.get(dtDefinition).add(taskDefinition);
			}
		}
		return taskDefinitionsMap;

	}
}
