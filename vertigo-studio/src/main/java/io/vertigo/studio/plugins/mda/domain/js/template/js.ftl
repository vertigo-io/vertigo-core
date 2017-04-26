/**
 * These metadata are generated automatically.
 * @type {Object}
 */
module.exports = {
    <#list dtDefinitions as dtDefinition>
    ${dtDefinition.classSimpleName?uncap_first}: {
        <#list dtDefinition.fields as dtField>
        ${dtField.camelCaseName}: {
            domain: "${dtField.domainName}",
            required: ${dtField.required?string("true","false")}
        }<#sep>,</#sep>
        </#list>
    }<#sep>,</#sep>
</#list>
};
