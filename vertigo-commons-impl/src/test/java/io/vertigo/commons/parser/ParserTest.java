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
	private static final Rule HELLO = new TermRule("hello");
	private static final Rule WORLD = new TermRule("world");
	private static final Rule MUSIC = new TermRule("music");
	private static final Rule SPACE = new TermRule(" ");
	private static final Rule FROM = new TermRule("from");
	private static final Rule WHERE = new WordRule(false, "abcdefghijklmnopqrstuvwxyz", WordRule.Mode.ACCEPT);
	private static final Rule PROPERTY = new WordRule(false, "\"", WordRule.Mode.REJECT_ESCAPABLE);
	private static final Rule AB = new TermRule("ab");
	//---
	private static final Rule<List<?>> MANY_AB = new ManyRule(AB, true, false);//=(AB, true) no global (can match abc)
	private static final Rule<List<?>> MANY_AB2 = new ManyRule(AB, true, true); //global (can't match abc)
	private static final Rule<List<?>> MANY_AB_MORE = new ManyRule(AB, false);

	private static final Rule<List<?>> HELLO_WORLD = new SequenceRule(
			HELLO,
			SPACE,
			WORLD);

	private static final Rule<Choice> WORLD_MUSIC = new FirstOfRule(
			WORLD,
			MUSIC);

	private static final Rule<List<?>> HELLO_WORLD_MUSIC = new SequenceRule(
			HELLO,
			SPACE,
			WORLD_MUSIC);

	private static final Rule<List<?>> HELLO_WORLD_FROM = new SequenceRule(
			HELLO,
			SPACE,
			WORLD,
			new OptionalRule<>(new SequenceRule(
					SPACE,
					FROM,
					SPACE,
					WHERE))//3
	);

	private static final Rule<List<?>> HELLO_PROPERTY = new SequenceRule(
			HELLO,
			SPACE,
			PROPERTY);

	@Test
	public void testTerm() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO
				.createParser()
				.parse("hello", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), cursor.getIndex());
		Assert.assertEquals("hello", cursor.getResult());
		//---
		final ParserCursor<List<?>> cursor2 = HELLO
				.createParser()
				.parse("hello, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), cursor2.getIndex());
		Assert.assertEquals("hello", cursor2.getResult());
	}

	@Test
	public void testPropertyEscapable() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_PROPERTY
				.createParser()
				.parse("hello \\\"mister\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello \\\"mister\\\"".length(), cursor.getIndex());
		Assert.assertEquals("\"mister\"", cursor.getResult().get(2));

		final ParserCursor<List<?>> cursor2 = HELLO_PROPERTY
				.createParser()
				.parse("hello mister\\\\truc\\\"hello\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello mister\\\\truc\\\"hello\\\"".length(), cursor2.getIndex());
		Assert.assertEquals("mister\\truc\"hello\"", cursor2.getResult().get(2));

	}

	@Test(expected = NotFoundException.class)
	public void testTermFail() throws NotFoundException {
		HELLO
				.createParser()
				.parse("Hi", 0);
	}

	@Test
	public void testSequence() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD
				.createParser()
				.parse("hello worlds", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), cursor.getIndex());
		Assert.assertEquals("hello", cursor.getResult().get(0));
		Assert.assertEquals("world", cursor.getResult().get(2));

		final ParserCursor<List<?>> cursor2 = HELLO_WORLD
				.createParser()
				.parse("hello world, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), cursor2.getIndex());
		Assert.assertEquals("hello", cursor2.getResult().get(0));
		Assert.assertEquals("world", cursor2.getResult().get(2));
	}

	@Test(expected = NotFoundException.class)
	public void testSequenceFail() throws NotFoundException {
		HELLO_WORLD
				.createParser()
				.parse("hello worms", 0);
		Assert.fail();
	}

	@Test
	public void testFirstOf() throws NotFoundException {
		final Choice choice = WORLD_MUSIC
				.createParser()
				.parse("world", 0)
				.getResult();
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		Assert.assertEquals(0, choice.getValue());
		Assert.assertEquals("world", choice.getResult());
		//---
		final Choice choice2 = WORLD_MUSIC
				.createParser()
				.parse("music", 0).getResult();
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		Assert.assertEquals(1, choice2.getValue());
		Assert.assertEquals("music", choice2.getResult());
	}

	@Test(expected = NotFoundException.class)
	public void testFirstOfFail() throws NotFoundException {
		WORLD_MUSIC.createParser()
				.parse("worm", 0);
	}

	@Test(expected = NotFoundException.class)
	public void testFirstOfFail2() throws NotFoundException {
		//On crée une liste vide de choix
		new FirstOfRule()
				.createParser()
				.parse("world", 0);
	}

	@Test
	public void testFirstOf2() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD_MUSIC
				.createParser()
				.parse("hello world, my name", 0);
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		final Choice choice = (Choice) cursor.getResult().get(2);
		Assert.assertEquals(0, choice.getValue());
		Assert.assertEquals("world", choice.getResult());
		//---
		final ParserCursor<List<?>> cursor2 = HELLO_WORLD_MUSIC
				.createParser()
				.parse("hello music, my name", 0);
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		final Choice choice2 = (Choice) cursor2.getResult().get(2);
		Assert.assertEquals(1, choice2.getValue());
		Assert.assertEquals("music", choice2.getResult());
	}

	@Test
	public void testOption() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD_FROM
				.createParser()
				.parse("hello world bla bla", 0);
		final Optional<List<?>> from = (Optional<List<?>>) cursor.getResult().get(3);
		Assert.assertFalse(from.isPresent());
		//---
		final ParserCursor<List<?>> cursor2 = HELLO_WORLD_FROM
				.createParser()
				.parse("hello world from mars", 0);
		final Optional<List<?>> from2 = (Optional<List<?>>) cursor2.getResult().get(3);
		Assert.assertTrue(from2.isPresent());
		Assert.assertEquals("mars", from2.get().get(3));
	}

	@Test
	public void testOptionFail() throws NotFoundException {
		final ParserCursor<List<?>> cursor = HELLO_WORLD_FROM
				.createParser()
				.parse("hello world from ", 0);

		final Optional<List<?>> from = (Optional<List<?>>) cursor.getResult().get(3);
		Assert.assertFalse(from.isPresent()); //pas d'exception NotFound
	}

	@Test
	public void testMany() throws NotFoundException {
		List<?> results = MANY_AB
				.createParser()
				.parse("", 0)
				.getResult();
		Assert.assertEquals(0, results.size());
		//-
		//		end = parser.parse("a", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size()); //ce cas ne match pas (ab)+
		//-
		results = MANY_AB
				.createParser()
				.parse("ab", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB
				.createParser()
				.parse("abc", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB
				.createParser()
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
		MANY_AB2
				.createParser()
				.parse("a", 0);
	}

	@Test(expected = NotFoundException.class)
	public void testManyGlobalFail2() throws NotFoundException {
		MANY_AB2
				.createParser()
				.parse("abc", 0);
	}

	@Test
	public void testManyFail2() throws NotFoundException {
		final List<?> results = MANY_AB
				.createParser()
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
				.createParser()
				.parse("ab", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB_MORE
				.createParser()
				.parse("abc", 0)
				.getResult();
		Assert.assertEquals(1, results.size());
		//-
		results = MANY_AB_MORE
				.createParser()
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
		MANY_AB_MORE
				.createParser()
				.parse("", 0);
	}

	@Test(expected = NotFoundException.class)
	public void testManyMoreFail2() throws NotFoundException {
		MANY_AB_MORE
				.createParser()
				.parse("a", 0);
	}

	public static void main(final String[] args) throws NotFoundException {
		parse(HELLO_WORLD_MUSIC, "hello music b");

		parse(HELLO_WORLD_FROM, "hello world");
		parse(HELLO_WORLD_FROM, "hello world from outerspace");

		//parse(MANY_A_MORE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

	}

	private static void parse(final Rule rule, final String text) throws NotFoundException {
		final ParserCursor cursor = rule
				.createParser()
				.parse(text, 0);
		System.out.println("======================================");
		System.out.println("text  : " + text);
		System.out.println("Règle : " + rule.getExpression());
		System.out.println("  reste     :" + text.substring(cursor.getIndex()));
		System.out.println("  elements  :" + cursor.getResult());
	}
}
