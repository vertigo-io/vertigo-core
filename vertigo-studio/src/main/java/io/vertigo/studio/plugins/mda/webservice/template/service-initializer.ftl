/*
    Ce fichier a été généré automatiquement.
    Toute modification sera perdue.
*/
/* tslint:disable */

import { container } from "focus4/ioc";
<#list serviceList as service>
import * as ${service.jsConstName}Service from "../../services/generated/${service.jsFileName}";
</#list>  

export function init() {
<#list serviceList as service>
	container.bind("${service.jsConstName}Service").toConstantValue(${service.jsConstName}Service);
</#list>  
}
