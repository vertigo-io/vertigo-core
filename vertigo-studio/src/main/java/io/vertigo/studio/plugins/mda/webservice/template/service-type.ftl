/*
    Ce fichier a été généré automatiquement.
    Toute modification sera perdue.
*/
/* tslint:disable */

<#list serviceList as service>
import * as ${service.jsConstName}Service from "./${service.jsFileName}";
</#list>  

<#list serviceList as service>
export type ${service.jsConstName?cap_first}Service = typeof ${service.jsConstName}Service;
</#list>  
