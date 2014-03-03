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
/*!! Assertion */
package io.vertigo.assertion;

import io.vertigo.kernel.lang.Assertion;

import org.junit.Test;


/*!
#	Assertion
Assertions has been designed to add **robustness** in your code.

In java, all is object ... and objects can be null. That's why java logs are filled with nullPointerExceptions.

We recommend

-	to avoid null objects
-	to check in every constructor and public/pacakage method if objects are not null.
To do this, you just have to use <code>Assertion.checkNotNull</code>

From our experience, strings are quite similar. An empty string is not null, but often does not make sense.

To check this, you can use <code>Assertion.checkNotEmpty</code> 


Each java method has

-	a contract
-	an implementation

Assertions should be used to valid contract.

In this case, you have to check all the arguments of the method : <code>Assertion.checkArgument</code>

-	is arg "name" in uppercase
-	is number between 0 and 100


An implementation can be seen as a process whith some inputs and an output.

Assertions should be used to valid the output

In this case, you have to use method <code>Assertion.checkState</code> 

-	is number i have processed between 0 and 100
-	is string i have processed non empty and contains less than 100 characters
 
#	Examples

*!- Plumbing 
*/
public class MDAssertion {
	@Test
	public void checkNotNull() {
		final Object myObject = new Object();
		/*! 
		##	Assertion.checkNotNull

		This assertion check if an object is null

		The following code throw NullPointerException if myObject is null
		 */
		Assertion.checkNotNull(myObject);

		/*! 
		 you can add an error message with parameters 
		 */
		String id = "id of myObject";
		Assertion.checkNotNull(myObject, "myObject identified by '{0}' can't be null", id);
		/*!- Plumbing */
	}

	@Test
	public void checkNotEmpty() {
		final String myString = "test";
		/*! 
		##	Assertion.checkArgNotEmpty 

		 throw IllegalStateException if myString is null or empty (blank characters : white space, \t, \n, \r, \p ...) 
		 */
		Assertion.checkArgNotEmpty(myString);

		/*! 
		 you can add an error message with parameters 
		 */
		String id = "id of myString";
		Assertion.checkNotNull(myString, "myString identified by '{0}' can't be null", id);
		/*!- Plumbing */
	}

	@Test
	public void checkArgument() {
		final Integer myWeight = 45;
		/*! 
		##	Assertion.checkArgument 
		throw IllegalArgumentException if condition is not valid 
		you must add an explicit error message
		
		*/
		Assertion.checkArgument(myWeight > 0, "weight '{0}' must be strictly positive", myWeight);
		/*!- Plumbing */
		displayPercent(50);
	}

	/*! 
	This type of assertion must be used just after every method declarartion. 
	*/
	public void displayPercent(double percent) {
		Assertion.checkArgument(percent > 0 && percent < 100, "percent '{0}' must be strictly between 0 and 100", percent);
		//----
		/*!- Plumbing */
	}

	@Test
	public void checkState() {
		int myStatus = 2;
		/*! 
		##	Assertion.checkState 
		throw IllegalStateException if condition is not valid 
		you must add an explicit error message
		
		*/
		Assertion.checkState(myStatus == 2 || myStatus == 3, "Status '{0}' must be in progress or closed", myStatus);
		/*!- Plumbing */
		buildPercentage();
	}

	/*! 
	This type of assertion can be used to check validity of a computation. 
	*/
	public void buildPercentage() {
		final double result; // here a complex calculation
		result = Math.sin(Math.cos(0.2)) * 50; // anyway result must be a percentage
		Assertion.checkState(result > 0 && result < 100, "percent '{0}' must be strictly between 0 and 100", result);
	}
	/*!- Plumbing */
}
