package ${dtDefinition.packageName};

import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données ${dtDefinition.classSimpleName}
 */
<#list annotations(dtDefinition.dtDefinition) as annotation>
${annotation}
</#list>
public final class ${dtDefinition.classSimpleName} implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	<#list dtDefinition.dtFields as dtField>
	private ${dtField.javaType} ${dtField.nameLowerCase?uncap_first};
	</#list>
	<#list dtDefinition.associations as association>
	<#if association.navigable>
		<#if association.multiple>
	private io.vertigo.dynamo.domain.model.DtList<${association.returnType}> ${association.role?uncap_first};
		<#else>
	private ${association.returnType} ${association.role?uncap_first};
		</#if>
	</#if>
	</#list>

	<#list dtDefinition.dtFields as dtField>
	/**
	 * Champ : ${dtField.type}.
	 * Récupère la valeur de la propriété '${dtField.display}'. 
	 * @return ${dtField.javaType} ${dtField.nameLowerCase?uncap_first} <#if dtField.notNull><b>Obligatoire</b></#if>
	 */
	<#list annotations(dtField.dtField, dtField.dtDefinition) as annotation>
	${annotation}
	</#list>
	public final ${dtField.javaType} get${dtField.nameLowerCase}() {
		return ${dtField.nameLowerCase?uncap_first};
	}

	/**
	 * Champ : ${dtField.type}.
	 * Définit la valeur de la propriété '${dtField.display}'.
	 * @param ${dtField.nameLowerCase?uncap_first} ${dtField.javaType} <#if dtField.notNull><b>Obligatoire</b></#if>
	 */
	public final void set${dtField.nameLowerCase}(final ${dtField.javaType} ${dtField.nameLowerCase?uncap_first}) {
		this.${dtField.nameLowerCase?uncap_first} = ${dtField.nameLowerCase?uncap_first};
	}

	</#list>
	<#list dtDefinition.dtComputedFields as dtField>
	/**
	 * Champ : ${dtField.type}.
	 * Récupère la valeur de la propriété calculée '${dtField.display}'. 
	 * @return ${dtField.javaType} ${dtField.nameLowerCase?uncap_first} <#if dtField.notNull><b>Obligatoire</b></#if>
	 */
	<#list annotations(dtField.dtField, dtField.dtDefinition) as annotation>
	${annotation}
	</#list>
	public final ${dtField.javaType} get${dtField.nameLowerCase}() {
		${dtField.javaCode}
	}

	</#list>
	<#if dtDefinition.associations?has_content>
	<#list dtDefinition.associations as association>
	<#if association.navigable>
	/**
	 * Association : ${association.label}.
	 * @return <#if association.multiple>io.vertigo.dynamo.domain.model.DtList<${association.returnType}><#else>${association.returnType}</#if>
	 */
	<#list annotations(association.associationNode) as annotation>
    ${annotation}
	</#list>
	<#if association.multiple>
	public final io.vertigo.dynamo.domain.model.DtList<${association.returnType}> get${association.role?cap_first}List() {
//		return this.<${association.returnType}> getList(get${association.role?cap_first}ListURI());
		final io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation fkDtListURI = get${association.role?cap_first}DtListURI();
		io.vertigo.kernel.lang.Assertion.checkNotNull(fkDtListURI);
		//---------------------------------------------------------------------
		//On est toujours dans un mode lazy.
		if (${association.role?uncap_first} == null) {
			${association.role?uncap_first} = io.vertigo.kernel.Home.getComponentSpace().resolve(io.vertigo.dynamo.persistence.PersistenceManager.class).getBroker().getList(fkDtListURI);
		}
		return ${association.role?uncap_first};
	}

	/**
	 * Association URI: ${association.label}.
	 * @return URI de l'association
	 */
	<#list annotations(association.associationNode) as annotation>
    ${annotation}
	</#list>
	public final io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation get${association.role?cap_first}DtListURI() {
		return io.vertigo.dynamo.domain.util.DtObjectUtil.createDtListURI(this, "${association.urn}", "${association.role}");
	}
	<#else>
	public final ${association.returnType} get${association.role?cap_first}() {
		final io.vertigo.dynamo.domain.model.URI<${association.returnType}> fkURI = get${association.role?cap_first}URI();
		if (fkURI == null) {
			return null;
		}
		//On est toujours dans un mode lazy.
		if (${association.role?uncap_first} == null) {
			${association.role?uncap_first} = io.vertigo.kernel.Home.getComponentSpace().resolve(io.vertigo.dynamo.persistence.PersistenceManager.class).getBroker().get(fkURI);
		}
		return ${association.role?uncap_first};
	}

	/**
	 * Retourne l'URI: ${association.label}.
	 * @return URI de l'association
	 */
	<#list annotations(association.associationNode) as annotation>
    ${annotation}
	</#list>
	public final io.vertigo.dynamo.domain.model.URI<${association.returnType}> get${association.role?cap_first}URI() {
		return io.vertigo.dynamo.domain.util.DtObjectUtil.createURI(this, "${association.urn}", ${association.returnType}.class);
	}
	</#if>
	<#else>

	// Association : ${association.label} non navigable
	</#if>
	</#list>
	<#else>
	//Aucune Association déclarée
	</#if>

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
