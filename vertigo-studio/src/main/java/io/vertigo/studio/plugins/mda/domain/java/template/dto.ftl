package ${dtDefinition.packageName};

import ${dtDefinition.stereotypePackageName};
<#if dtDefinition.entity || dtDefinition.fragment>
import io.vertigo.dynamo.domain.model.URI;
</#if>
<#if dtDefinition.simpleVisibleAssociation>
import io.vertigo.dynamo.domain.model.VAccessor;
</#if>	
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 */
 @Generated
<#list annotations(dtDefinition.dtDefinition) as annotation>
${annotation}
</#list>
public final class ${dtDefinition.classSimpleName} implements ${dtDefinition.stereotypeInterfaceName} {
	private static final long serialVersionUID = 1L;

	<#list dtDefinition.fields as dtField>
		<#if dtField.foreignKey>
	private final VAccessor<${dtField.association.returnType}> ${dtField.association.role?uncap_first}Accessor = new VAccessor(${dtField.association.returnType}.class);
		<#else>
	private ${dtField.javaType} ${dtField.upperCamelCaseName?uncap_first};
		</#if>
	</#list>
	<#list dtDefinition.associations as association>
		<#if association.navigable>
			<#if association.multiple>
	private io.vertigo.dynamo.domain.model.DtList<${association.returnType}> ${association.role?uncap_first};
			</#if>
		</#if>
	</#list>
	<#if dtDefinition.entity>

	/** {@inheritDoc} */
		<#list annotations("URI") as annotation>
	${annotation}
		</#list>
	@Override
	public URI<${dtDefinition.classSimpleName}> getURI() {
		return DtObjectUtil.createURI(this);
	}
	</#if>
	<#if dtDefinition.fragment>

	/** {@inheritDoc} */
		<#list annotations("URI") as annotation>
	${annotation}
		</#list>
	@Override
	public URI<${dtDefinition.entityClassSimpleName}> getEntityURI() {
		return DtObjectUtil.createEntityURI(this); 
	}
	</#if>

	<#list dtDefinition.fields as dtField>
	/**
	 * Champ : ${dtField.type}.
	 * Récupère la valeur de la propriété '${dtField.display}'.
	 * @return ${dtField.javaType} ${dtField.upperCamelCaseName?uncap_first}<#if dtField.required> <b>Obligatoire</b></#if>
	 */
		<#list annotations(dtField) as annotation>
	${annotation}
		</#list>
	public ${dtField.javaType} get${dtField.upperCamelCaseName}() {
		<#if dtField.foreignKey>
		return (${dtField.javaType})  ${dtField.association.role?uncap_first}Accessor.getId();
		<#else> 
		return ${dtField.upperCamelCaseName?uncap_first};
		</#if>
	}

	/**
	 * Champ : ${dtField.type}.
	 * Définit la valeur de la propriété '${dtField.display}'.
	 * @param ${dtField.upperCamelCaseName?uncap_first} ${dtField.javaType}<#if dtField.required> <b>Obligatoire</b></#if>
	 */
	public void set${dtField.upperCamelCaseName}(final ${dtField.javaType} ${dtField.upperCamelCaseName?uncap_first}) {
		<#if dtField.foreignKey>
		${dtField.association.role?uncap_first}Accessor.setId(${dtField.upperCamelCaseName?uncap_first});
		<#else> 
		this.${dtField.upperCamelCaseName?uncap_first} = ${dtField.upperCamelCaseName?uncap_first};
		</#if>
	}

	</#list>
	<#list dtDefinition.dtComputedFields as dtField>
	/**
	 * Champ : ${dtField.type}.
	 * Récupère la valeur de la propriété calculée '${dtField.display}'.
	 * @return ${dtField.javaType} ${dtField.upperCamelCaseName?uncap_first}<#if dtField.required> <b>Obligatoire</b></#if>
	 */
		<#list annotations(dtField) as annotation>
	${annotation}
		</#list>
	public ${dtField.javaType} get${dtField.upperCamelCaseName}() {
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
				<#if association.multiple>
	public io.vertigo.dynamo.domain.model.DtList<${association.returnType}> get${association.role?cap_first}List() {
		// On doit avoir une clé primaire renseignée. Si ce n'est pas le cas, on renvoie une liste vide
		if (io.vertigo.dynamo.domain.util.DtObjectUtil.getId(this) == null) {
			return new io.vertigo.dynamo.domain.model.DtList<>(${association.returnType}.class);
		}
		final io.vertigo.dynamo.domain.model.DtListURI fkDtListURI = get${association.role?cap_first}DtListURI();
		io.vertigo.lang.Assertion.checkNotNull(fkDtListURI);
		//---------------------------------------------------------------------
		//On est toujours dans un mode lazy.
		if (${association.role?uncap_first} == null) {
			${association.role?uncap_first} = io.vertigo.app.Home.getApp().getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class).getDataStore().findAll(fkDtListURI);
		}
		return ${association.role?uncap_first};
	}

	/**
	 * Association URI: ${association.label}.
	 * @return URI de l'association
	 */
					<#list annotations(association.definition) as annotation>
	${annotation}
					</#list>
	public io.vertigo.dynamo.domain.metamodel.association.DtListURIFor<#if association.simple>Simple<#else>NN</#if>Association get${association.role?cap_first}DtListURI() {
		return io.vertigo.dynamo.domain.util.DtObjectUtil.createDtListURIFor<#if association.simple>Simple<#else>NN</#if>Association(this, "${association.urn}", "${association.role}");
	}

				<#else>
	public ${association.returnType} get${association.role?cap_first}() {
		return ${association.role?uncap_first}Accessor.get();
	}

	/**
	 * Retourne l'URI: ${association.label}.
	 * @return URI de l'association
	 */
					<#list annotations(association.definition) as annotation>
	${annotation}
					</#list>
	public io.vertigo.dynamo.domain.model.URI<${association.returnType}> get${association.role?cap_first}URI() {
		return ${association.role?uncap_first}Accessor.getURI();
	}

				</#if>
			</#if>
		</#list>
	</#if>

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
