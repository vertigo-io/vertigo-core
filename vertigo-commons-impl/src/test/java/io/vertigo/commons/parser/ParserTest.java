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
	private static final Rule MANY_AB = new ManyRule(AB, true, false);//=(AB, true) no global (can match abc)
	private static final Rule MANY_AB2 = new ManyRule(AB, true, true); //global (can't match abc)
	private static final Rule MANY_AB_MORE = new ManyRule(AB, false);

	private static final Rule HELLO_WORLD = new SequenceRule(
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

	private static final Rule HELLO_WORLD_FROM = new SequenceRule(
			HELLO,
			SPACE,
			WORLD,
			new OptionalRule<>(new SequenceRule(
					SPACE,
					FROM,
					SPACE,
					WHERE))//3
	);

	private static final Rule HELLO_PROPERTY = new SequenceRule(
			HELLO,
			SPACE,
			PROPERTY);

	@Test
	public void testTerm() throws NotFoundException {
		final Parser<List<?>> parser = HELLO.createParser();
		//---
		int end;
		end = parser.parse("hello", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), end);
		Assert.assertEquals("hello", parser.get());
		//---
		end = parser.parse("hello, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello"
		Assert.assertEquals("hello".length(), end);
		Assert.assertEquals("hello", parser.get());

	}

	@Test
	public void testPropertyEscapable() throws NotFoundException {
		final Parser<List<?>> parser = HELLO_PROPERTY.createParser();
		//---
		int end;
		end = parser.parse("hello \\\"mister\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello \\\"mister\\\"".length(), end);
		Assert.assertEquals("\"mister\"", parser.get().get(2));

		end = parser.parse("hello mister\\\\truc\\\"hello\\\"", 0);
		//On vérifie que l'on a trouvé la chaine "mister"
		Assert.assertEquals("hello mister\\\\truc\\\"hello\\\"".length(), end);
		Assert.assertEquals("mister\\truc\"hello\"", parser.get().get(2));

	}

	@Test(expected = NotFoundException.class)
	public void testTermFail() throws NotFoundException {
		final Parser<List<?>> parser = HELLO.createParser();
		//---
		parser.parse("Hi", 0);
		Assert.fail();
	}

	@Test
	public void testSequence() throws NotFoundException {
		final Parser<List<?>> parser = HELLO_WORLD.createParser();
		//---
		int end;
		end = parser.parse("hello worlds", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), end);
		Assert.assertEquals("hello", parser.get().get(0));
		Assert.assertEquals("world", parser.get().get(2));

		end = parser.parse("hello world, my name is", 0);
		//On vérifie que l'on a trouvé la chaine "hello world"
		Assert.assertEquals("hello world".length(), end);
		Assert.assertEquals("hello", parser.get().get(0));
		Assert.assertEquals("world", parser.get().get(2));
	}

	@Test(expected = NotFoundException.class)
	public void testSequenceFail() throws NotFoundException {
		final Parser<List<?>> parser = HELLO_WORLD.createParser();
		//---
		parser.parse("hello worms", 0);
		Assert.fail();
	}

	@Test
	public void testFirstOf() throws NotFoundException {
		final Parser<Choice> parser = WORLD_MUSIC.createParser();
		//---
		parser.parse("world", 0);
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		Assert.assertEquals(0, parser.get().getValue());
		Assert.assertEquals("world", parser.get().getResult());
		//---
		parser.parse("music", 0);
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		Assert.assertEquals(1, parser.get().getValue());
		Assert.assertEquals("music", parser.get().getResult());
	}

	@Test(expected = NotFoundException.class)
	public void testFirstOfFail() throws NotFoundException {
		final Parser<Choice> parser = WORLD_MUSIC.createParser();
		//---
		parser.parse("worm", 0);
		Assert.fail();
	}

	@Test(expected = NotFoundException.class)
	public void testFirstOfFail2() throws NotFoundException {
		//On crée une liste vide de choix
		final Parser<Choice> parser = new FirstOfRule().createParser();
		//---
		parser.parse("world", 0);
		Assert.fail();
	}

	@Test
	public void testFirstOf2() throws NotFoundException {
		final Parser<List<?>> parser = HELLO_WORLD_MUSIC.createParser();
		//---
		Choice choice;
		//-
		parser.parse("hello world, my name", 0);
		//On vérifie que l'on a trouvé la chaine "world" qui correspond au cas 0
		choice = (Choice) parser.get().get(2);
		Assert.assertEquals(0, choice.getValue());
		Assert.assertEquals("world", choice.getResult());
		//---
		parser.parse("hello music, my name", 0);
		//On vérifie que l'on a trouvé la chaine "music" qui correspond au cas 1
		choice = (Choice) parser.get().get(2);
		Assert.assertEquals(1, choice.getValue());
		Assert.assertEquals("music", choice.getResult());
	}

	@Test
	public void testOption() throws NotFoundException {
		final Parser<List<?>> parser = HELLO_WORLD_FROM.createParser();
		//---
		Optional<List<?>> from;

		//-
		parser.parse("hello world bla bla", 0);
		from = (Optional<List<?>>) parser.get().get(3);
		Assert.assertFalse(from.isPresent());
		//-
		parser.parse("hello world from mars", 0);
		from = (Optional<List<?>>) parser.get().get(3);
		Assert.assertTrue(from.isPresent());
		Assert.assertEquals("mars", from.get().get(3));
	}

	@Test
	public void testOptionFail() throws NotFoundException {
		final Parser<List<?>> parser = HELLO_WORLD_FROM.createParser();
		//---
		Optional<List<?>> from;

		//-
		parser.parse("hello world from ", 0);
		from = (Optional<List<?>>) parser.get().get(3);
		Assert.assertFalse(from.isPresent()); //pas d'exception NotFound
	}

	@Test
	public void testMany() throws NotFoundException {
		final Parser<List<?>> parser = MANY_AB.createParser();
		//---
		List results;
		//-
		parser.parse("", 0);
		results = parser.get();
		Assert.assertEquals(0, results.size());
		//-
		//		end = parser.parse("a", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size()); //ce cas ne match pas (ab)+
		//-
		parser.parse("ab", 0);
		results = parser.get();
		Assert.assertEquals(1, results.size());
		//-
		parser.parse("abc", 0);
		results = parser.get();
		Assert.assertEquals(1, results.size());
		//-
		parser.parse("abababab", 0);
		results = parser.get();
		Assert.assertEquals(4, results.size());
		Assert.assertEquals("ab", results.get(0));
		Assert.assertEquals("ab", results.get(1));
		Assert.assertEquals("ab", results.get(2));
		Assert.assertEquals("ab", results.get(3));
	}

	@Test(expected = NotFoundException.class)
	public void testManyGlobalFail() throws NotFoundException {
		final Parser<List<?>> parser = MANY_AB2.createParser();
		//---
		/*int end =*/parser.parse("a", 0);
		Assert.fail();
	}

	@Test(expected = NotFoundException.class)
	public void testManyGlobalFail2() throws NotFoundException {
		final Parser<List<?>> parser = MANY_AB2.createParser();
		//---
		/*int end =*/parser.parse("abc", 0);
		Assert.fail();
	}

	@Test
	public void testManyFail2() throws NotFoundException {
		final Parser<List<?>> parser = MANY_AB.createParser();
		//---
		/*int end =*/parser.parse("abc", 0);
		final List results = parser.get();
		Assert.assertEquals(1, results.size());
		Assert.assertEquals("ab", results.get(0));
	}

	@Test
	public void testManyMore() throws NotFoundException {
		final Parser<List<?>> parser = MANY_AB_MORE.createParser();
		//---
		List results;
		//-
		//		end = parser.parse("", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size());
		//		//-
		//		end = parser.parse("a", 0);
		//		results = parser.get();
		//		Assert.assertEquals(0, results.size());
		//-
		parser.parse("ab", 0);
		results = parser.get();
		Assert.assertEquals(1, results.size());
		//-
		parser.parse("abc", 0);
		results = parser.get();
		Assert.assertEquals(1, results.size());
		//-
		parser.parse("abababab", 0);
		results = parser.get();
		Assert.assertEquals(4, results.size());
		Assert.assertEquals("ab", results.get(0));
		Assert.assertEquals("ab", results.get(1));
		Assert.assertEquals("ab", results.get(2));
		Assert.assertEquals("ab", results.get(3));
	}

	@Test(expected = NotFoundException.class)
	public void testManyMoreFail() throws NotFoundException {
		final Parser<List<?>> parser = MANY_AB_MORE.createParser();
		//---
		parser.parse("", 0);
		Assert.fail();
	}

	@Test(expected = NotFoundException.class)
	public void testManyMoreFail2() throws NotFoundException {
		final Parser<List<?>> parser = MANY_AB_MORE.createParser();
		//---
		parser.parse("a", 0);
		Assert.fail();
	}

	public static void main(final String[] args) throws NotFoundException {
		parse(HELLO_WORLD_MUSIC, "hello music b");

		parse(HELLO_WORLD_FROM, "hello world");
		parse(HELLO_WORLD_FROM, "hello world from outerspace");

		//parse(MANY_A_MORE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

	}

	private static void parse(final Rule rule, final String text) throws NotFoundException {
		final Parser parser = rule.createParser();
		final int end = parser.parse(text, 0);
		System.out.println("======================================");
		System.out.println("text  : " + text);
		System.out.println("Règle : " + rule.getExpression());
		System.out.println("  reste     :" + text.substring(end));
		System.out.println("  elements  :" + parser.get());
	}
}
