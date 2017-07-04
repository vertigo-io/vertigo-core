/*
    Ce fichier a été généré automatiquement.
    Toute modification sera perdue.
*/
/* tslint:disable */

<#list serviceList as service>
import * as ${service.jsConstName?uncap_first}Service from "./${service.jsFileName}";
</#list>  

<#list serviceList as service>
export type ${service.jsConstName}Service = typeof ${service.jsConstName?uncap_first}Service;
</#list>  
