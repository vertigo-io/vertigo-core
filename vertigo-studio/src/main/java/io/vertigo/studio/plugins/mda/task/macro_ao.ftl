 
<#macro generateBody taskDefinitions>

	/**
	 * Création d'une tache.
	 * @param task Type de la tache
	 * @return Builder de la tache
	 */
	private static TaskBuilder createTaskBuilder(final String name) {
		final TaskDefinition taskDefinition = Home.getApp().getDefinitionSpace().resolve(name, TaskDefinition.class);
		return new TaskBuilder(taskDefinition);
	}

<#list taskDefinitions as taskDefinition>
	/**
	 * Execute la tache ${taskDefinition.name}.
	<#list taskDefinition.inAttributes as taskAttribute>
	 * @param ${taskAttribute.variableName} ${taskAttribute.dataType} <#if !taskAttribute.required>(peut être null)</#if>
	</#list>
     <#if taskDefinition.out>
	 * @return <#if !taskDefinition.outAttribute.required>Option de </#if>${taskDefinition.outAttribute.dataType} ${taskDefinition.outAttribute.variableName}
	</#if>
	*/
	public <#if taskDefinition.out><#if !taskDefinition.outAttribute.required>Option<</#if>${taskDefinition.outAttribute.dataType}<#if !taskDefinition.outAttribute.required>></#if><#else>void</#if> ${taskDefinition.methodName}(<#list taskDefinition.inAttributes as taskAttribute>final <#if !taskAttribute.required>Option<</#if>${taskAttribute.dataType}<#if !taskAttribute.required>></#if> ${taskAttribute.variableName}<#if taskAttribute_has_next>, </#if></#list>) {
		final Task task = createTaskBuilder("${taskDefinition.name}")
	<#list taskDefinition.inAttributes as taskAttribute>
				.addValue("${taskAttribute.name}", ${taskAttribute.variableName}<#if !taskAttribute.required>.getOrElse(null)</#if>)
    </#list>
				.build();
    <#if taskDefinition.out>
		<#if !taskDefinition.outAttribute.required>
		return Option.option((${taskDefinition.outAttribute.dataType})getTaskManager()
				.execute(task)
				.getResult());
		<#else>
		return getTaskManager()
				.execute(task)
				.getResult();
		</#if>
	 <#else>
		getTaskManager().execute(task);
    </#if>
	}

</#list>
</#macro>
