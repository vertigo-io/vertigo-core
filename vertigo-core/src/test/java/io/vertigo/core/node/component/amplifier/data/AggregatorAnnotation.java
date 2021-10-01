/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.component.amplifier.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertigo.core.node.component.amplifier.AmplifierMethodAnnotation;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@AmplifierMethodAnnotation

/**
 * This annotation is used to mark a method that will be proxied.
 *
 * @author pchretien
 */
public @interface AggregatorAnnotation {

	/**
	 * @return the operation that will be applied to the args of the method
	 */
	AggregatorOperation operation();
}
