/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.peg;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public final class ParserTest {
	private static final PegRule<String> HELLO = PegRules.term("hello");
	private static final PegRule<String> WORLD = PegRules.term("world");
	private static final PegRule<String> MUSIC = PegRules.term("music");
	private static final PegRule<String> SPACE = PegRules.term(" ");
	private static final PegRule<String> FROM = PegRules.term("from");
	private static final PegRule<String> WHERE = PegRules.word(false, "abcdefghijklmnopqrstuvwxyz", PegWordRule.Mode.ACCEPT, "where");
	private static final PegRule<String> PROPERTY = PegRules.word(false, "\"", PegWordRule.Mode.REJECT_ESCAPABLE, "property");
	private static final PegRule<String> AB = PegRules.term("ab");
	//---
	private static final PegRule<List<String>> MANY_AB = PegRules.zeroOrMore(AB, false);//=(AB, true) no global (can match abc)
	private static final PegRule<List<String>> MANY_AB2 = PegRules.zeroOrMore(AB, true); //global (can't match abc)
	private static final PegRule<List<String>> MANY_AB_MORE = PegRules.oneOrMore(AB, false);

	private static final PegRule<List<?>> HELLO_WORLD = PegRules.sequence(
			HELLO,
			SPACE,
			WORLD);

	private static final PegRule<PegChoice> WORLD_MUSIC = PegRules.choice(
			WORLD,
			MUSIC);

	private static final PegRule<List<?>> HELLO_WORLD_MUSIC = PegRules.sequence(
			HELLO,
			SPACE,
			WORLD_MUSIC);

	private static final PegRule<List<?>> HELLO_WORLD_FROM = PegRules.sequence(
			HELLO,
			SPACE,
			WORLD,
			PegRules.optional(PegRules.sequence(
					SPACE,
					FROM,
					SPACE,
					WHERE))//3
	);

	private static final PegRule<List<?>> HELLO_PROPERTY = PegRules.sequence(
			HELLO,
			SPACE,
			PROPERTY);

	@Test
	public void testTerm() throws PegNoMatchFoundException {
		final PegResult<String> cursor = HELLO
				.parse("hello", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), cursor.getIndex());
		Assert.assertEquals("hello", cursor.getValue());
		//---
		final PegResult<String> cursor2 = HELLO
				.parse("hello, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), cursor2.getIndex());
		Assert.assertEquals("hello", cursor2.getValue());
	}

	@Test
	public void testPropertyEscapable() throws PegNoMatchFoundException {
		final PegResult<List<?>> cursor = HELLO_PROPERTY
				.parse("hello \\\"mister\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello \\\"mister\\\"".length(), cursor.getIndex());
		Assert.assertEquals("\"mister\"", cursor.getValue().get(2));

		final PegResult<List<?>> cursor2 = HELLO_PROPERTY
				.parse("hello mister\\\\truc\\\"hello\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello mister\\\\truc\\\"hello\\\"".length(), cursor2.getIndex());
		Assert.assertEquals("mister\\truc\"hello\"", cursor2.getValue().get(2));

	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testTermFail() throws PegNoMatchFoundException {
		HELLO.parse("Hi", 0);
	}

	@Test
	public void testSequence() throws PegNoMatchFoundException {
		final PegResult<List<?>> cursor = HELLO_WORLD
				.parse("hello worlds", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), cursor.getIndex());
		Assert.assertEquals("hello", cursor.getValue().get(0));
		Assert.assertEquals("world", cursor.getValue().get(2));

		final PegResult<List<?>> cursor2 = HELLO_WORLD
				.parse("hello world, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), cursor2.getIndex());
		Assert.assertEquals("hello", cursor2.getValue().get(0));
		Assert.assertEquals("world", cursor2.getValue().get(2));
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testSequenceFail() throws PegNoMatchFoundException {
		HELLO_WORLD
				.parse("hello worms", 0);
		Assert.fail();
	}

	@Test
	public void testFirstOf() throws PegNoMatchFoundException {
		final PegChoice choice = WORLD_MUSIC
				.parse("world", 0)
				.getValue();
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		Assert.assertEquals(0, choice.getChoiceIndex());
		Assert.assertEquals("world", choice.getValue());
		//---
		final PegChoice choice2 = WORLD_MUSIC
				.parse("music", 0).getValue();
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		Assert.assertEquals(1, choice2.getChoiceIndex());
		Assert.assertEquals("music", choice2.getValue());
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testFirstOfFail() throws PegNoMatchFoundException {
		WORLD_MUSIC.parse("worm", 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChoiceEmptyFailed() throws PegNoMatchFoundException {
		//An empty list of choices
		PegRules.choice()
				.parse("world", 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChoiceOneFailed() throws PegNoMatchFoundException {
		PegRules.choice(HELLO)
				.parse("world", 0);
	}

	@Test
	public void testChoice() throws PegNoMatchFoundException {
		final PegResult<List<?>> cursor = HELLO_WORLD_MUSIC
				.parse("hello world, my name", 0);
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		final PegChoice choice = (PegChoice) cursor.getValue().get(2);
		Assert.assertEquals(0, choice.getChoiceIndex());
		Assert.assertEquals("world", choice.getValue());
		//---
		final PegResult<List<?>> cursor2 = HELLO_WORLD_MUSIC
				.parse("hello music, my name", 0);
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		final PegChoice choice2 = (PegChoice) cursor2.getValue().get(2);
		Assert.assertEquals(1, choice2.getChoiceIndex());
		Assert.assertEquals("music", choice2.getValue());
	}

	@Test
	public void testOption() throws PegNoMatchFoundException {
		final PegResult<List<?>> cursor = HELLO_WORLD_FROM
				.parse("hello world bla bla", 0);
		final Optional<List<?>> from = (Optional<List<?>>) cursor.getValue().get(3);
		Assert.assertFalse(from.isPresent());
		//---
		final PegResult<List<?>> cursor2 = HELLO_WORLD_FROM
				.parse("hello world from mars", 0);
		final Optional<List<?>> from2 = (Optional<List<?>>) cursor2.getValue().get(3);
		Assert.assertTrue(from2.isPresent());
		Assert.assertEquals("mars", from2.get().get(3));
	}

	@Test
	public void testOptionFail() throws PegNoMatchFoundException {
		final PegResult<List<?>> cursor = HELLO_WORLD_FROM
				.parse("hello world from ", 0);

		final Optional<List<?>> from = (Optional<List<?>>) cursor.getValue().get(3);
		Assert.assertFalse(from.isPresent()); //pas d'exception NotFound
	}

	@Test
	public void testMany() throws PegNoMatchFoundException {
		List<?> results = MANY_AB
				.parse("", 0)
				.getValue();
		Assert.assertEquals(0, results.size());
		//-
		//		end = parser.parse("a", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size()); //ce cas ne match pas (ab)+
		//-
		results = MANY_AB
				.parse("ab", 0)
				.getValue();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB
				.parse("abc", 0)
				.getValue();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB
				.parse("abababab", 0)
				.getValue();
		Assert.assertEquals(4, results.size());
		Assert.assertEquals("ab", results.get(0));
		Assert.assertEquals("ab", results.get(1));
		Assert.assertEquals("ab", results.get(2));
		Assert.assertEquals("ab", results.get(3));
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testManyGlobalFail() throws PegNoMatchFoundException {
		MANY_AB2.parse("a", 0);
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testManyGlobalFail2() throws PegNoMatchFoundException {
		MANY_AB2.parse("abc", 0);
	}

	@Test
	public void testManyFail2() throws PegNoMatchFoundException {
		final List<?> results = MANY_AB
				.parse("abc", 0)
				.getValue();
		Assert.assertEquals(1, results.size());
		Assert.assertEquals("ab", results.get(0));
	}

	@Test
	public void testManyMore() throws PegNoMatchFoundException {
		//-
		//		end = parser.parse("", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size());
		//		//-
		//		end = parser.parse("a", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size());
		//-
		List<?> results = MANY_AB_MORE
				.parse("ab", 0)
				.getValue();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB_MORE
				.parse("abc", 0)
				.getValue();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB_MORE
				.parse("abababab", 0)
				.getValue();

		Assert.assertEquals(4, results.size());
		Assert.assertEquals("ab", results.get(0));
		Assert.assertEquals("ab", results.get(1));
		Assert.assertEquals("ab", results.get(2));
		Assert.assertEquals("ab", results.get(3));
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testManyMoreFail() throws PegNoMatchFoundException {
		MANY_AB_MORE.parse("", 0);
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testManyMoreFail2() throws PegNoMatchFoundException {
		MANY_AB_MORE.parse("a", 0);
	}

	public static void main(final String[] args) throws PegNoMatchFoundException {
		parse(HELLO_WORLD_MUSIC, "hello music b");

		parse(HELLO_WORLD_FROM, "hello world");
		parse(HELLO_WORLD_FROM, "hello world from outerspace");

		//parse(MANY_A_MORE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

	}

	private static void parse(final PegRule<?> rule, final String text) throws PegNoMatchFoundException {
		final PegResult<?> cursor = rule
				.parse(text, 0);
		System.out.println("======================================");
		System.out.println("text  : " + text);
		System.out.println("Règle : " + rule.getExpression());
		System.out.println("  reste     :" + text.substring(cursor.getIndex()));
		System.out.println("  elements  :" + cursor.getValue());
	}
}
