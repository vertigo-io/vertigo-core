/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.rest.rest;

import io.vertigo.rest.rest.validation.DtObjectValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RestfullService marker.
 * Inspired from javax.ws.rs : http://docs.oracle.com/javaee/6/api/index.html?javax/ws/rs/package-summary.html
 * and vert.x
 * @author npiedeloup
 */
public interface RestfulService {

	/**
	 * Path prefix.
	 */
	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PathPrefix {
		String value();
	}

	/**
	 * Documentation.
	 */
	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Doc {
		String value();
	}

	/**
	 * Accept anonymous access.
	 */
	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AnonymousAccessAllowed {
		//
	}

	/**
	 * No session access.
	 */
	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SessionLess {
		//
	}

	/**
	 * Invalidate session (so logout).
	 * (done AFTER request, so there is a session created)
	 */
	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SessionInvalidate {
		//
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface GET {
		/**
		 * Defines a URI template for the resource class or method, must not include matrix parameters.
		 * Embedded template parameters are allowed and are of the form:
		 *  	param = "{" *WSP name *WSP [ ":" *WSP regex *WSP ] "}"
		 *  	name = (ALPHA / DIGIT / "_")*(ALPHA / DIGIT / "." / "_" / "-" ) ; \w[\w\.-]*
		 *  	regex = *( nonbrace / "{" *nonbrace "}" ) ; where nonbrace is any char other than "{" and "}"
		 * See href="http://tools.ietf.org/html/rfc5234">RFC 5234 for a description of the syntax used above and the expansions of WSP, ALPHA and DIGIT. 
		 * In the above name is the template parameter name and the optional regex specifies the contents of the capturing group for the parameter. 
		 * If regex is not supplied then a default value of [^/]+ which terminates at a path segment boundary, is used. 
		 * Matching of request URIs to URI templates is performed against encoded path values and implementations will not escape literal characters in regex automatically, 
		 * therefore any literals in regex should be escaped by the author according to the rules of href="http://tools.ietf.org/html/rfc3986.section-3.3">RFC 3986 section 3.3. 
		 * Caution is recommended in the use of regex, incorrect use can lead to a template parameter matching unexpected URI paths. 
		 * See href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Pattern.html">Pattern for further information on the syntax of regular expressions. 
		 * Values of template parameters may be extracted using PathParam.
		 * The literal part of the supplied value (those characters that are not part of a template parameter) is automatically percent encoded to conform to the path production of href="http://tools.ietf.org/html/rfc3986.section-3.3">RFC 3986 section 3.3. Note that percent encoded values are allowed in the literal part of the value, an implementation will recognize such values and will not double encode the '%' character.
		 **/
		String value();
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface POST {
		String value();
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface DELETE {
		String value();
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PUT {
		String value();
	}

	@Target({ ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PathParam {
		String value();
	}

	@Target({ ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryParam {
		String value();
	}

	@Target({ ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Validate {
		Class<? extends DtObjectValidator>[] value();
	}

	@Target({ ElementType.PARAMETER, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ExcludedFields {
		String[] value();
	}

	@Target({ ElementType.PARAMETER, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface IncludedFields {
		String[] value();
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AccessTokenPublish {
		//empty
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AccessTokenMandatory {
		//empty
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AccessTokenConsume {
		//empty
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ServerSideSave {
		//empty
	}

	@Target({ ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ServerSideRead {
		//empty
	}

	@Target({ ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ServerSideConsume {
		//empty
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AutoSortAndPagination {
		//empty
	}

	@Target({ ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface InnerBodyParam { //Only if mutiple Body Params
		String value();
	}

}
