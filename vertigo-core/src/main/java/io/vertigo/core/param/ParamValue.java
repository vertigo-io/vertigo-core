/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the name of a parameter.
 *
 * This annotation can be used on fields or parameters to indicate the name of a configuration parameter.
 * The value of the annotation represents the name of the parameter.
 *
 * Example usage:
 *
 * @ParamValue("connectionName")
 * private String connectionName;
 *
 * @author: pchretien
 *
 * @see io.vertigo.core.param.ParamManager
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamValue {

	/**
     * The name of the configuration parameter.
     *
     * @return the name of the parameter
     */
	String value() default "";
}
