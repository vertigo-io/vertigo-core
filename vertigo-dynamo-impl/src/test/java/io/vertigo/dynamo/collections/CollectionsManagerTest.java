/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.collections.data.DtDefinitions;
import io.vertigo.dynamo.collections.data.domain.Item;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.domain.util.VCollectors;

/**
 * @author pchretien
 */
//non final, to be overrided for previous lib version
public class CollectionsManagerTest extends AbstractTestCaseJU4 {
	private static final String Ba_aa = "Ba aa";
	private static final String aaa_ba = "aaa ba";
	private static final String bb_aa = "bb aa";
	private DtDefinition dtDefinitionItem;
	@Inject
	private CollectionsManager collectionsManager;

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() {
		dtDefinitionItem = DtObjectUtil.findDtDefinition(Item.class);
	}

	@Test
	public void testHeavySort() {
		// final DtList<Item> sortDtc;
		final DtList<Item> dtc = createItems();
		//
		for (int i = 0; i < 50000; i++) {
			final Item mocka = new Item();
			mocka.setLabel(String.valueOf(i % 100));
			dtc.add(mocka);
		}

		final DtList<Item> sortedDtc = collectionsManager.sort(dtc, "LABEL", false);

		nop(sortedDtc);

	}

	@Test
	public void testSort() {
		DtList<Item> sortDtc;
		final DtList<Item> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "LABEL", false);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "LABEL", true);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { null, bb_aa, Ba_aa, aaa_ba }, extractLabels(sortDtc));
	}

	@Test
	public void testNumericSort() {
		DtList<Item> sortDtc;
		final DtList<Item> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "ID", false);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { Ba_aa, null, aaa_ba, bb_aa }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "ID", true);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { bb_aa, aaa_ba, null, Ba_aa }, extractLabels(sortDtc));
	}

	@Test
	public void testFilter() {
		final DtList<Item> result = createItems()
				.stream()
				.filter(Criterions.isEqualTo(DtDefinitions.Fields.LABEL, aaa_ba).toPredicate())
				.collect(VCollectors.toDtList(Item.class));
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testFilterTwoValues() {
		final Predicate<Item> filterA = Criterions.isEqualTo(DtDefinitions.Fields.LABEL, "aaa").toPredicate();
		final Predicate<Item> filterB = Criterions.isEqualTo(DtDefinitions.Fields.ID, 13L).toPredicate();

		final DtList<Item> result = createItemsForRangeTest()
				.stream()
				.filter(filterA.and(filterB))
				.collect(VCollectors.toDtList(Item.class));
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testFilterFullText() {
		final DtList<Item> result = collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.filter("aa", 1000, dtDefinitionItem.getFields())
				.build()
				.apply(createItems());
		Assert.assertEquals(3, result.size(), 0);

	}

	@Test
	public void testFilterFullTextTokenizer() {
		final DtList<Item> dtc = createItems();
		final Collection<DtField> searchedDtFields = dtDefinitionItem.getFields();
		final Item mock1 = new Item();
		mock1.setId(seqId++);
		mock1.setLabel("Agence de l'Ouest");
		dtc.add(mock1);

		final Item mock2 = new Item();
		mock2.setId(seqId++);
		mock2.setLabel("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche n'est pas case insensitive", filter(dtc, "agence", 1000, searchedDtFields).size() == 1);//majuscule/minuscule
		Assert.assertTrue("La recherche n'est pas plain text", filter(dtc, "l'ouest", 1000, searchedDtFields).size() == 1);//tokenizer
		Assert.assertTrue("La recherche ne supporte pas les accents", filter(dtc, "hopital", 1000, searchedDtFields).size() == 1);//accents
		Assert.assertTrue("La recherche ne supporte pas les caractères spéciaux fr (ç)", filter(dtc, "cava", 1000, searchedDtFields).size() == 1); //accents fr (ç)
		Assert.assertTrue("La recherche ne supporte pas les caractères spéciaux latin1 (ã)", filter(dtc, "apied", 1000, searchedDtFields).size() == 1); //accents autre (ã)
		Assert.assertTrue("La recherche ne supporte pas la recherche par préfix", filter(dtc, "apie", 1000, searchedDtFields).size() == 1);//prefix
	}

	private List<Item> filter(final DtList<Item> dtc, final String query, final int nbRows, final Collection<DtField> searchedDtFields) {
		return collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.filter(query, nbRows, searchedDtFields)
				.build()
				.apply(dtc);
	}

	@Test
	public void testFilterFullTextElision() {
		final DtList<Item> dtc = createItems();
		final Collection<DtField> searchedDtFields = dtDefinitionItem.getFields();

		final Item mock1 = new Item();
		mock1.setId(seqId++);
		mock1.setLabel("Agence de l'Ouest");
		dtc.add(mock1);

		final Item mock2 = new Item();
		mock2.setId(seqId++);
		mock2.setLabel("Hôpital et autres accents çava où àpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche ne supporte pas l'elision", filter(dtc, "ouest", 1000, searchedDtFields).size() == 1);
	}

	@Test
	public void testFilterFullTextMultiKeyword() {
		final DtList<Item> dtc = createItems();
		final Collection<DtField> searchedDtFields = dtDefinitionItem.getFields();

		final Item mock1 = new Item();
		mock1.setId(seqId++);
		mock1.setLabel("Agence de l'Ouest");
		dtc.add(mock1);

		final Item mock2 = new Item();
		mock2.setId(seqId++);
		mock2.setLabel("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche ne supporte pas l'espace", filter(dtc, "agence de", 1000, searchedDtFields).size() == 1);//mots proches
		Assert.assertTrue("La recherche ne supporte pas l'utilisation de plusieurs mots", filter(dtc, "hopital accent", 1000, searchedDtFields).size() == 1);//mots séparés
		Assert.assertTrue("La recherche ne supporte pas l'inversion des mots", filter(dtc, "accent hopital", 1000, searchedDtFields).size() == 1);//inversés
		Assert.assertTrue("Les mots clés ne sont pas en 'ET'", filter(dtc, "agence hopital", 1000, searchedDtFields).size() == 0);//multi doc
	}

	/**
	 * Vérifie le comportement quand la recherche en commence par addresse trop de term du dictionnaire.
	 * Par défaut Lucene envoi une erreur TooMany...., le collectionsManager limite aux premiers terms.
	 */
	@Test
	public void testFilterFullTextBigList() {
		final UnaryOperator<DtList<Item>> filterFunction = collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.filter("a", 2000, dtDefinitionItem.getFields())
				.build();
		Assert.assertNotNull(filterFunction);
		final DtList<Item> bigFamillyList = new DtList<>(Item.class);
		for (int i = 0; i < 50000; i++) {
			final Item mocka = new Item();
			mocka.setId(seqId++);
			mocka.setLabel("blabla a" + (char) ('a' + i % 26) + String.valueOf(i % 100));
			bigFamillyList.add(mocka);
		}
		final DtList<Item> result = filterFunction.apply(bigFamillyList);
		Assert.assertEquals(2000, result.size(), 0);
	}

	@Test
	public void testSortWithIndex() {
		DtList<Item> sortDtc;
		final DtList<Item> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		sortDtc = collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.sort("LABEL", false)
				.build()
				.apply(dtc);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));

		// ======================== Descendant
		sortDtc = collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.sort("LABEL", true)
				.build()
				.apply(dtc);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { null, bb_aa, Ba_aa, aaa_ba }, extractLabels(sortDtc));
	}

	@Test
	public void testNumericSortWithIndex() {
		DtList<Item> sortDtc;
		final DtList<Item> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.sort("ID", false)
				.build()
				.apply(dtc);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { Ba_aa, null, aaa_ba, bb_aa }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.sort("ID", true)
				.build()
				.apply(dtc);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { bb_aa, aaa_ba, null, Ba_aa }, extractLabels(sortDtc));
	}

	@Test
	public void testSubListWithIndex() {
		// on test une implémentation de référence ArrayList
		final List<String> list = Arrays.asList("a", "b");
		Assert.assertEquals(0, list.subList(0, 0).size());
		Assert.assertEquals(2, list.subList(0, 2).size()); // >0, 1
		Assert.assertEquals(1, list.subList(1, 2).size()); // >1
		Assert.assertEquals(0, list.subList(2, 2).size());
		// on teste notre implémentation
		//can't test subList(0,0) : illegal argument
		Assert.assertEquals(2, subListWithIndex(createItems(), 0, 2).size());
		Assert.assertEquals(1, subListWithIndex(createItems(), 1, 2).size());
		//can't test subList(2,2) : illegal argument
	}

	private DtList<Item> subListWithIndex(final DtList<Item> dtc, final int start, final int end) {
		return collectionsManager.<Item> createIndexDtListFunctionBuilder()
				.filterSubList(start, end)
				.build()
				.apply(dtc);
	}

	@Test
	public void testSubList() {
		// on test une implémentation de référence ArrayList
		final List<String> list = Arrays.asList("a", "b");
		Assert.assertEquals(0, list.subList(0, 0).size());
		Assert.assertEquals(2, list.subList(0, 2).size()); // >0, 1
		Assert.assertEquals(1, list.subList(1, 2).size()); // >1
		Assert.assertEquals(0, list.subList(2, 2).size());
		// on teste notre implémentation
		Assert.assertEquals(0, subList(createItems(), 0, 0).size());
		Assert.assertEquals(2, subList(createItems(), 0, 2).size());
		Assert.assertEquals(1, subList(createItems(), 1, 2).size());
		Assert.assertEquals(0, subList(createItems(), 2, 2).size());
	}

	private DtList<Item> subList(final DtList<Item> dtc, final int start, final int end) {
		return dtc
				.stream()
				.skip(start)
				.limit(end - start)
				.collect(VCollectors.toDtList(dtc.getDefinition()));
	}

	/**
	 * combiner sort/filter ; filter/sort ; sublist/sort ; filter/sublist.
	 *
	 */
	@Test
	public void testChainFilterSortSubList() {

		final DtList<Item> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		final Predicate<Item> predicate = Criterions.isEqualTo(DtDefinitions.Fields.LABEL, aaa_ba).toPredicate();
		final Function<DtList<Item>, DtList<Item>> sort = (list) -> collectionsManager.sort(list, "LABEL", false);

		final int sizeDtc = dtc.size();

		DtList<Item> sortDtc, filterDtc, subList;
		// ======================== sort/filter
		sortDtc = sort.apply(dtc);
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));
		filterDtc = sortDtc.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(Item.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sort/sublist
		sortDtc = sort.apply(dtc);
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));
		subList = subList(sortDtc, 0, sizeDtc - 1);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== filter/sort
		filterDtc = dtc.stream().filter(predicate).collect(VCollectors.toDtList(Item.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));
		sortDtc = sort.apply(filterDtc);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== filter/sublist
		filterDtc = dtc.stream().filter(predicate).collect(VCollectors.toDtList(Item.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));
		subList = subList(filterDtc, 0, filterDtc.size() - 1);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sublist/sort
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba_aa, null, aaa_ba }, extractLabels(subList));
		sortDtc = sort.apply(subList);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sublist/filter
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba_aa, null, aaa_ba }, extractLabels(subList));
		filterDtc = subList.stream().filter(predicate).collect(VCollectors.toDtList(Item.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// === dtc non modifié
		assertEquals(indexDtc, extractLabels(dtc));

	}

	@Test
	public void testCreateFilterForValue() {
		final Predicate predicate = collectionsManager
				.filter(ListFilter.of("LABEL" + ":\"aaa\""));
		Assert.assertNotNull(predicate);
	}

	@Test
	public void testTermFilterString() {
		testTermFilter("LABEL:\"aaa\"", 2);
		testTermFilter("LABEL:\"aaab\"", 1);
	}

	@Test
	public void testTermFilterLong() {
		testTermFilter("ID:\"1\"", 1);
		testTermFilter("ID:\"11\"", 1);
		testTermFilter("ID:\"2\"", 0);
	}

	@Test
	public void testCreateFilter() {
		final Predicate<DtObject> predicate = collectionsManager.filter(ListFilter.of("LABEL" + ":[a TO b]"));
		Assert.assertNotNull(predicate);
	}

	@Test
	public void testRangeFilter() {
		testRangeFilter("LABEL" + ":[a TO b]", 5);
	}

	@Test
	public void testRangeFilterLong() {
		testRangeFilter("ID:[1 TO 10]", 3);
		testRangeFilter("ID:[1 TO 10[", 2);
		testRangeFilter("ID:]1 TO 10]", 2);
		testRangeFilter("ID:]1 TO 10[", 1);
		testRangeFilter("ID:]1 TO *[", 9);
		testRangeFilter("ID:[* TO *[", 10);
	}

	@Test
	public void testRangeFilterString() {
		testRangeFilter("LABEL:[a TO b]", 5);
		testRangeFilter("LABEL:[* TO c[", 7);
		testRangeFilter("LABEL:[* TO c]", 8);
		testRangeFilter("LABEL:[* TO cb]", 9);
		testRangeFilter("LABEL:[aaab TO aaac]", 2);
		testRangeFilter("LABEL:[aaab TO aaac[", 1);
	}

	private void testTermFilter(final String filterString, final int countEspected) {
		final DtList<Item> result = createItemsForRangeTest()
				.stream()
				.filter(collectionsManager.<Item> filter(ListFilter.of(filterString)))
				.collect(VCollectors.toDtList(Item.class));

		Assert.assertEquals(countEspected, result.size());
	}

	private void testRangeFilter(final String filterString, final int countEspected) {
		final Predicate<Item> predicate = collectionsManager
				.filter(ListFilter.of(filterString));
		Assert.assertNotNull(predicate);
		final DtList<Item> result = createItemsForRangeTest().stream().filter(predicate).collect(VCollectors.toDtList(Item.class));
		Assert.assertEquals(countEspected, result.size());
	}

	private static DtList<Item> createItemsForRangeTest() {
		final DtList<Item> dtc = createItems();

		final Item mock1 = new Item();
		mock1.setId(1L);
		mock1.setLabel("aaab");
		dtc.add(mock1);

		final Item mock2 = new Item();
		mock2.setId(10L);
		mock2.setLabel("aaac");
		dtc.add(mock2);

		final Item mock3 = new Item();
		mock3.setId(11L);
		mock3.setLabel("caaa");
		dtc.add(mock3);

		final Item mock4 = new Item();
		mock4.setId(12L);
		mock4.setLabel("aaa");
		dtc.add(mock4);

		final Item mock5 = new Item();
		mock5.setId(13L);
		mock5.setLabel("aaa");
		dtc.add(mock5);

		final Item mock6 = new Item();
		mock6.setId(3L);
		mock6.setLabel("c");
		dtc.add(mock6);

		return dtc;
	}

	/**
	 * Asserts that two booleans are equal.
	 *
	 */
	private static void assertEquals(final String[] expected, final String[] actual) {
		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	private static String[] extractLabels(final DtList<Item> dtc) {
		final String[] index = new String[dtc.size()];
		for (int i = 0; i < dtc.size(); i++) {
			index[i] = dtc.get(i).getLabel();
		}
		return index;
	}

	private static long seqId = 100;

	private static DtList<Item> createItems() {
		final DtList<Item> dtc = new DtList<>(Item.class);
		// les index sont données par ordre alpha > null à la fin >
		final Item mockB = new Item();
		mockB.setId(seqId++);
		mockB.setLabel(Ba_aa);
		dtc.add(mockB);

		final Item mockNull = new Item();
		mockNull.setId(seqId++);
		// On ne renseigne pas le Label > null
		dtc.add(mockNull);

		final Item mocka = new Item();
		mocka.setId(seqId++);
		mocka.setLabel(aaa_ba);
		dtc.add(mocka);

		final Item mockb = new Item();
		mockb.setId(seqId++);
		mockb.setLabel(bb_aa);
		dtc.add(mockb);

		// On crée et on supprimme un élément dans la liste pour vérifier
		// l'intégrité de la liste (Par rapport aux null).
		final Item mockRemoved = new Item();
		mockRemoved.setId(seqId++);
		mockRemoved.setLabel("mockRemoved");
		dtc.add(mockRemoved);

		dtc.remove(mockRemoved);
		return dtc;
	}
}
