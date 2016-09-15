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
package io.vertigo.commons.parser;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public final class ParserTest {
	private static final Rule HELLO = Rules.term("hello");
	private static final Rule WORLD = Rules.term("world");
	private static final Rule MUSIC = Rules.term("music");
	private static final Rule SPACE = Rules.term(" ");
	private static final Rule FROM = Rules.term("from");
	private static final Rule WHERE = Rules.word(false, "abcdefghijklmnopqrstuvwxyz", WordRule.Mode.ACCEPT);
	private static final Rule PROPERTY = Rules.word(false, "\"", WordRule.Mode.REJECT_ESCAPABLE);
	private static final Rule<String> AB = Rules.term("ab");
	//---
	private static final Rule<List<String>> MANY_AB = Rules.zeroOrMore(AB, false);//=(AB, true) no global (can match abc)
	private static final Rule<List<String>> MANY_AB2 = Rules.zeroOrMore(AB, true); //global (can't match abc)
	private static final Rule<List<String>> MANY_AB_MORE = Rules.oneOrMore(AB, false);

	private static final Rule<List<?>> HELLO_WORLD = Rules.sequence(
			HELLO,
			SPACE,
			WORLD);

	private static final Rule<Choice> WORLD_MUSIC = Rules.firstOf(
			WORLD,
			MUSIC);

	private static final Rule<List<?>> HELLO_WORLD_MUSIC = Rules.sequence(
			HELLO,
			SPACE,
			WORLD_MUSIC);

	private static final Rule<List<?>> HELLO_WORLD_FROM = Rules.sequence(
			HELLO,
			SPACE,
			WORLD,
			new OptionalRule<>(Rules.sequence(
					SPACE,
					FROM,
					SPACE,
					WHERE))//3
	);

	private static final Rule<List<?>> HELLO_PROPERTY = Rules.sequence(
			HELLO,
			SPACE,
			PROPERTY);

	@Test
	public void testTerm() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO
				.parse("hello", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), cursor.getIndex());
		Assert.assertEquals("hello", cursor.getResult());
		//---
		final ParserCursor<List<?>> cursor2 = HELLO
				.parse("hello, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), cursor2.getIndex());
		Assert.assertEquals("hello", cursor2.getResult());
	}

	@Test
	public void testPropertyEscapable() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_PROPERTY
				.parse("hello \\\"mister\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello \\\"mister\\\"".length(), cursor.getIndex());
		Assert.assertEquals("\"mister\"", cursor.getResult().get(2));

		final ParserCursor<List<?>> cursor2 = HELLO_PROPERTY
				.parse("hello mister\\\\truc\\\"hello\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello mister\\\\truc\\\"hello\\\"".length(), cursor2.getIndex());
		Assert.assertEquals("mister\\truc\"hello\"", cursor2.getResult().get(2));

	}

	@Test(expected = NotFoundException.class)
	public void testTermFail() throws NotFoundException {
		HELLO.parse("Hi", 0);
	}

	@Test
	public void testSequence() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD
				.parse("hello worlds", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), cursor.getIndex());
		Assert.assertEquals("hello", cursor.getResult().get(0));
		Assert.assertEquals("world", cursor.getResult().get(2));

		final ParserCursor<List<?>> cursor2 = HELLO_WORLD
				.parse("hello world, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), cursor2.getIndex());
		Assert.assertEquals("hello", cursor2.getResult().get(0));
		Assert.assertEquals("world", cursor2.getResult().get(2));
	}

	@Test(expected = NotFoundException.class)
	public void testSequenceFail() throws NotFoundException {
		HELLO_WORLD
				.parse("hello worms", 0);
		Assert.fail();
	}

	@Test
	public void testFirstOf() throws NotFoundException {
		final Choice choice = WORLD_MUSIC
				.parse("world", 0)
				.getResult();
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		Assert.assertEquals(0, choice.getValue());
		Assert.assertEquals("world", choice.getResult());
		//---
		final Choice choice2 = WORLD_MUSIC
				.parse("music", 0).getResult();
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		Assert.assertEquals(1, choice2.getValue());
		Assert.assertEquals("music", choice2.getResult());
	}

	@Test(expected = NotFoundException.class)
	public void testFirstOfFail() throws NotFoundException {
		WORLD_MUSIC.parse("worm", 0);
	}

	@Test(expected = NotFoundException.class)
	public void testFirstOfFail2() throws NotFoundException {
		//On crée une liste vide de choix
		Rules.firstOf()
				.parse("world", 0);
	}

	@Test
	public void testFirstOf2() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD_MUSIC
				.parse("hello world, my name", 0);
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		final Choice choice = (Choice) cursor.getResult().get(2);
		Assert.assertEquals(0, choice.getValue());
		Assert.assertEquals("world", choice.getResult());
		//---
		final ParserCursor<List<?>> cursor2 = HELLO_WORLD_MUSIC
				.parse("hello music, my name", 0);
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		final Choice choice2 = (Choice) cursor2.getResult().get(2);
		Assert.assertEquals(1, choice2.getValue());
		Assert.assertEquals("music", choice2.getResult());
	}

	@Test
	public void testOption() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD_FROM
				.parse("hello world bla bla", 0);
		final Optional<List<?>> from = (Optional<List<?>>) cursor.getResult().get(3);
		Assert.assertFalse(from.isPresent());
		//---
		final ParserCursor<List<?>> cursor2 = HELLO_WORLD_FROM
				.parse("hello world from mars", 0);
		final Optional<List<?>> from2 = (Optional<List<?>>) cursor2.getResult().get(3);
		Assert.assertTrue(from2.isPresent());
		Assert.assertEquals("mars", from2.get().get(3));
	}

	@Test
	public void testOptionFail() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD_FROM
				.parse("hello world from ", 0);

		final Optional<List<?>> from = (Optional<List<?>>) cursor.getResult().get(3);
		Assert.assertFalse(from.isPresent()); //pas d'exception NotFound
	}

	@Test
	public void testMany() throws NotFoundException {
		List<?> results = MANY_AB
				.parse("", 0)
				.getResult();
		Assert.assertEquals(0, results.size());
		//-
		//		end = parser.parse("a", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size()); //ce cas ne match pas (ab)+
		//-
		results = MANY_AB
				.parse("ab", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB
				.parse("abc", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB
				.parse("abababab", 0)
				.getResult();
		Assert.assertEquals(4, results.size());
		Assert.assertEquals("ab", results.get(0));
		Assert.assertEquals("ab", results.get(1));
		Assert.assertEquals("ab", results.get(2));
		Assert.assertEquals("ab", results.get(3));
	}

	@Test(expected = NotFoundException.class)
	public void testManyGlobalFail() throws NotFoundException {
		MANY_AB2.parse("a", 0);
	}

	@Test(expected = NotFoundException.class)
	public void testManyGlobalFail2() throws NotFoundException {
		MANY_AB2.parse("abc", 0);
	}

	@Test
	public void testManyFail2() throws NotFoundException {
		final List<?> results = MANY_AB
				.parse("abc", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		Assert.assertEquals("ab", results.get(0));
	}

	@Test
	public void testManyMore() throws NotFoundException {
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
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB_MORE
				.parse("abc", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB_MORE
				.parse("abababab", 0)
				.getResult();

		Assert.assertEquals(4, results.size());
		Assert.assertEquals("ab", results.get(0));
		Assert.assertEquals("ab", results.get(1));
		Assert.assertEquals("ab", results.get(2));
		Assert.assertEquals("ab", results.get(3));
	}

	@Test(expected = NotFoundException.class)
	public void testManyMoreFail() throws NotFoundException {
		MANY_AB_MORE.parse("", 0);
	}

	@Test(expected = NotFoundException.class)
	public void testManyMoreFail2() throws NotFoundException {
		MANY_AB_MORE.parse("a", 0);
	}

	public static void main(final String[] args) throws NotFoundException {
		parse(HELLO_WORLD_MUSIC, "hello music b");

		parse(HELLO_WORLD_FROM, "hello world");
		parse(HELLO_WORLD_FROM, "hello world from outerspace");

		//parse(MANY_A_MORE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

	}

	private static void parse(final Rule<?> rule, final String text) throws NotFoundException {
		final ParserCursor<?> cursor = rule
				.parse(text, 0);
		System.out.println("======================================");
		System.out.println("text  : " + text);
		System.out.println("Règle : " + rule.getExpression());
		System.out.println("  reste     :" + text.substring(cursor.getIndex()));
		System.out.println("  elements  :" + cursor.getResult());
	}
}
