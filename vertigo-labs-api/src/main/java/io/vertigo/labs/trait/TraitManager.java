package io.vertigo.labs.trait;

import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.Option;

public interface TraitManager extends Manager {
	<T extends Trait> Option<T> findTrait(Class<T> traitClass, String subjectId);

	<T extends Trait> void putTrait(Class<T> traitClass, String subjectId, T trait);

	<T extends Trait> void deleteTrait(Class<T> traitClass, String subjectId);

	//List<Trait> findAllTraits(String subjectId);

}
