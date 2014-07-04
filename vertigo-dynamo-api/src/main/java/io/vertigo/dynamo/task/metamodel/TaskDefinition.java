package io.vertigo.dynamo.task.metamodel;

import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;
import io.vertigo.kernel.metamodel.Prefix;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Définition d'une tache et de ses attributs.
 *
 * @author  fconstantin, pchretien
 */
@Prefix("TK")
public final class TaskDefinition implements Definition {
	/** Nom de la définition. */
	private final String name;

	/** Nom sans prefix de la définition. */
	private final String localName;

	/** Nom du package. */
	private final String packageName;

	/** Chaine de configuration du service. */
	private final String request;

	/** Map des (Nom, TaskAttribute) définissant les attributs de tache. */
	private final Map<String, TaskAttribute> taskAttributeMap;

	/**
	 * Moyen de réaliser la tache.
	 */
	private final Class<? extends TaskEngine> taskEngineClass;

	/**
	 * Constructeur
	 * @param taskEngineClass Classe réalisant l'implémentation
	 * @param request Chaine de configuration
	 */
	TaskDefinition(final String name, final String packageName, final Class<? extends TaskEngine> taskEngineClass, final String request, final List<TaskAttribute> taskAttributes) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(taskEngineClass, "La class du TaskEngine est obligatoire");
		Assertion.checkNotNull(request, "La request est obligatoire");
		Assertion.checkNotNull(taskAttributes);
		//----------------------------------------------------------------------
		this.name = name;
		localName = DefinitionUtil.getLocalName(name, TaskDefinition.class);
		this.packageName = packageName;
		this.request = request;
		taskAttributeMap = createMap(taskAttributes);
		this.taskEngineClass = taskEngineClass;
	}

	/**
	 * Création  d'une Map non modifiable.
	 * @param taskAttributes Attributs de la tache
	 */
	private static Map<String, TaskAttribute> createMap(final List<TaskAttribute> taskAttributes) {
		final Map<String, TaskAttribute> map = new LinkedHashMap<>();
		for (final TaskAttribute taskAttribute : taskAttributes) {
			Assertion.checkNotNull(taskAttribute);
			Assertion.checkArgument(!map.containsKey(taskAttribute.getName()), "attribut {0} existe déjà", taskAttribute.getName());
			//----------------------------------------------------------------------
			map.put(taskAttribute.getName(), taskAttribute);
		}
		return java.util.Collections.unmodifiableMap(map);
	}

	/**
	 * Retourne l'attribut de la tache identifié par son nom.
	 *
	 * @param attributeName Nom de l'attribut recherché.
	 * @return Définition de l'attribut.
	 */
	public TaskAttribute getAttribute(final String attributeName) {
		Assertion.checkNotNull(attributeName);
		//----------------------------------------------------------------------
		final TaskAttribute taskAttribute = taskAttributeMap.get(attributeName);
		Assertion.checkNotNull(taskAttribute, "nom d''attribut :{0} non trouvé pour le service :{1}", attributeName, this);
		return taskAttribute;
	}

	/**
	 * Retourne si l'attribut fait partie de l'API de la tache.
	 * @param attributeName Nom de l'attribut
	 * @return Si l'attribut fait partie de l'API de la tache
	 */
	public boolean containsAttribute(final String attributeName) {
		return taskAttributeMap.containsKey(attributeName);
	}

	/**
	 * Retourne la classe réalisant l'implémentation de la tache.
	 *
	 * @return Classe réalisant l'implémentation
	 */
	public Class<? extends TaskEngine> getTaskEngineClass() {
		return taskEngineClass;
	}

	/**
	 * Retourne la String de configuration de la tache.
	 * Cette méthode est utilisée par le TaskEngine.
	 *
	 * @return Configuration de la tache.
	 */
	public String getRequest() {
		return request;
	}

	/**
	 * Retourne la liste des attributs de la tache sous forme d'une Collection
	 * de TaskAttribute.
	 *
	 * @return Liste des attributs de la tache
	 */
	public Collection<TaskAttribute> getAttributes() {
		return taskAttributeMap.values();
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Nom de la définition sans prefix (XXX_YYYY).
	 */
	public String getLocalName() {
		return localName;
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
