package io.vertigo.labs.trait;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.kernel.lang.Option;
import io.vertigo.labs.trait.Commenting;
import io.vertigo.labs.trait.TraitManager;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public class TraitManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private TraitManager traitManager;

	@Test
	public void loadEmptyData() {
		traitManager.deleteTrait(Commenting.class, "45");
		Option<Commenting> commenting = traitManager.findTrait(Commenting.class, "45");
		Assert.assertTrue(commenting.isEmpty());
	}

	@Test
	public void putData() {
		Commenting commenting = new Commenting();
		commenting.setComments("my nice comment");
		traitManager.putTrait(Commenting.class, "45", commenting);

		Option<Commenting> commenting2 = traitManager.findTrait(Commenting.class, "45");
		Assert.assertTrue(commenting2.isDefined());
	}
}
